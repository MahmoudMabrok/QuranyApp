#!/usr/bin/env python3
"""Summarize Qurany release artifacts: size, dex, baseline profile, R8."""

import argparse
import json
import struct
import zipfile
from pathlib import Path


def human(n):
    n = float(n)
    for unit in ("B", "KB", "MB"):
        if abs(n) < 1024 or unit == "MB":
            return f"{n:.1f} {unit}" if unit != "B" else f"{int(n)} B"
        n /= 1024
    return f"{n:.1f} MB"


def pct(new, old):
    if old is None:
        return "—"
    if old == 0:
        return "جديد" if new else "0%"
    delta = (new - old) / old * 100
    sign = "+" if delta > 0 else ""
    return f"{sign}{delta:.1f}%"


def dex_header(data):
    if len(data) < 112 or data[:3] != b"dex":
        return None
    def u32(off):
        return struct.unpack_from("<I", data, off)[0]
    return {
        "bytes": len(data),
        "strings": u32(56),
        "types": u32(64),
        "fields": u32(80),
        "methods": u32(88),
        "classes": u32(96),
    }


def _dex_sort(name):
    stem = name[len("classes"):-len(".dex")]
    return int(stem) if stem.isdigit() else 0


def zip_entries(path):
    with zipfile.ZipFile(path) as z:
        return {i.filename: i for i in z.infolist()}


def analyze_apk(path):
    if path is None or not Path(path).is_file():
        return None
    path = Path(path)
    infos = zip_entries(path)
    dex = []
    with zipfile.ZipFile(path) as z:
        names = sorted(
            (n for n in infos if n.startswith("classes") and n.endswith(".dex")),
            key=_dex_sort,
        )
        for name in names:
            header = dex_header(z.read(name))
            if header:
                header["name"] = name
                dex.append(header)
        prof = infos.get("assets/dexopt/baseline.prof") or infos.get("baseline.prof")
        profm = infos.get("assets/dexopt/baseline.profm") or infos.get("baseline.profm")
    assets = sum(i.file_size for n, i in infos.items() if n.startswith("assets/"))
    return {
        "path": str(path),
        "apk_bytes": path.stat().st_size,
        "uncompressed": sum(i.file_size for i in infos.values()),
        "assets_uncompressed": assets,
        "dex": dex,
        "dex_bytes": sum(d["bytes"] for d in dex),
        "methods": sum(d["methods"] for d in dex),
        "classes": sum(d["classes"] for d in dex),
        "baseline_prof": prof.file_size if prof else 0,
        "baseline_profm": profm.file_size if profm else 0,
    }


def analyze_aab(path):
    if path is None or not Path(path).is_file():
        return None
    path = Path(path)
    infos = zip_entries(path)
    startup = []
    r8_note = "r8.json not in the bundle"
    with zipfile.ZipFile(path) as z:
        r8_names = [n for n in infos if n.endswith("r8.json")]
        if r8_names:
            try:
                payload = json.loads(z.read(r8_names[0]))
            except json.JSONDecodeError:
                payload = None
            r8_note = r8_names[0]
            startup = _startup_dexes(payload)
        prof = 0
        for name, info in infos.items():
            if name.endswith("baseline.prof"):
                prof += info.file_size
    return {
        "path": str(path),
        "aab_bytes": path.stat().st_size,
        "r8_json": r8_note,
        "startup_dexes": startup,
        "baseline_prof": prof,
    }


def _startup_dexes(node):
    found = []
    if isinstance(node, dict):
        if "startup" in node or "name" in node and str(node.get("name", "")).endswith(".dex"):
            if "startup" in node or node.get("name", "").endswith(".dex"):
                found.append({
                    "name": node.get("name") or node.get("filename") or "?",
                    "startup": bool(node.get("startup", False)),
                })
        for value in node.values():
            found.extend(_startup_dexes(value))
    elif isinstance(node, list):
        for value in node:
            found.extend(_startup_dexes(value))
    # Dedup while keeping order.
    seen = set()
    out = []
    for item in found:
        key = (item["name"], item["startup"])
        if key in seen or item["name"] in ("?", ""):
            continue
        seen.add(key)
        out.append(item)
    return out


def r8_usage(mapping_dir):
    if not mapping_dir:
        return None
    usage = Path(mapping_dir) / "usage.txt"
    resources = Path(mapping_dir) / "resources.txt"
    out = {"removed_classes": 0, "removed_members": 0, "unused_resources": 0}
    if usage.is_file():
        for line in usage.read_text(errors="replace").splitlines():
            if not line or line.startswith("#"):
                continue
            if line[:1].isspace():
                out["removed_members"] += 1
            elif line.endswith(":"):
                out["removed_classes"] += 1
    if resources.is_file():
        for line in resources.read_text(errors="replace").splitlines():
            stripped = line.strip()
            if stripped.startswith("res/") or "/res/" in stripped:
                out["unused_resources"] += 1
    if not usage.is_file() and not resources.is_file():
        return None
    return out


def _row(label, head, base, fmt=human):
    if head is None:
        return None
    base_cell = fmt(base) if base is not None else "—"
    delta = pct(head, base) if base is not None else "—"
    return f"| {label} | {fmt(head)} | {base_cell} | {delta} |"


def render(head_apk, base_apk, head_aab, base_aab, head_debug, head_r8, download):
    lines = [
        "<!-- qurany-perf-numbers -->",
        "## أرقام من بناء Qurany",
        "",
        "اتقاست من الـ artifacts، مش من أرقام وثائق Android العامة.",
        "",
    ]
    if head_apk is None:
        lines.append("مفيش APK للـ release. شوف لوج البناء.")
        return "\n".join(lines) + "\n"

    lines += [
        "| المقياس | الفرع ده (R8 + startup/baseline) | master | الفرق |",
        "| --- | ---: | ---: | ---: |",
    ]
    rows = [
        _row("حجم release APK (base)", head_apk["apk_bytes"], base_apk["apk_bytes"] if base_apk else None),
        _row("DEX الكلي", head_apk["dex_bytes"], base_apk["dex_bytes"] if base_apk else None),
        _row("classes.dex (أول DEX)", head_apk["dex"][0]["bytes"] if head_apk["dex"] else None,
             base_apk["dex"][0]["bytes"] if base_apk and base_apk["dex"] else None),
        _row("عدد الميثودز في الـ DEX", head_apk["methods"], base_apk["methods"] if base_apk else None, fmt=lambda n: f"{n:,}"),
        _row("عدد الكلاسات في الـ DEX", head_apk["classes"], base_apk["classes"] if base_apk else None, fmt=lambda n: f"{n:,}"),
        _row("أصول غير مضغوطة (assets)", head_apk["assets_uncompressed"],
             base_apk["assets_uncompressed"] if base_apk else None),
        _row("baseline.prof", head_apk["baseline_prof"], base_apk["baseline_prof"] if base_apk else None),
        _row("baseline.profm", head_apk["baseline_profm"], base_apk["baseline_profm"] if base_apk else None),
    ]
    if head_aab:
        rows.append(_row("حجم الـ AAB", head_aab["aab_bytes"], base_aab["aab_bytes"] if base_aab else None))
    if download.get("head"):
        hmin, hmax = download["head"]
        b = download.get("base")
        rows.append(_row("تقدير تحميل Play (min)", hmin, b[0] if b else None))
        rows.append(_row("تقدير تحميل Play (max)", hmax, b[1] if b else None))
    lines.extend(row for row in rows if row)

    lines += ["", "### DEX في release الفرع ده", "",
              "| الملف | الحجم | كلاسات | ميثودز |", "| --- | ---: | ---: | ---: |"]
    for dex in head_apk["dex"]:
        lines.append(
            f"| `{dex['name']}` | {human(dex['bytes'])} | {dex['classes']:,} | {dex['methods']:,} |"
        )
    lines.append("")
    lines.append(
        f"الـ baseline profile جوه الـ APK: **{'موجود' if head_apk['baseline_prof'] else 'مش موجود'}** "
        f"({human(head_apk['baseline_prof'])})."
    )
    if base_apk and not base_apk["baseline_prof"] and head_apk["baseline_prof"]:
        lines.append("master من غير `baseline.prof`. الفرع ده أول نسخة بتحطه.")

    if head_debug:
        saved = head_debug["apk_bytes"] - head_apk["apk_bytes"]
        word = "أصغر" if saved >= 0 else "أكبر"
        lines += [
            "",
            "### أثر R8 (نفس الكود، debug من غير minify مقابل release)",
            "",
            "| | debug | release | الفرق |",
            "| --- | ---: | ---: | ---: |",
            f"| حجم الـ APK | {human(head_debug['apk_bytes'])} | {human(head_apk['apk_bytes'])} | {human(abs(saved))} {word} ({pct(head_apk['apk_bytes'], head_debug['apk_bytes'])}) |",
            f"| DEX | {human(head_debug['dex_bytes'])} | {human(head_apk['dex_bytes'])} | {pct(head_apk['dex_bytes'], head_debug['dex_bytes'])} |",
            f"| ميثودز | {head_debug['methods']:,} | {head_apk['methods']:,} | {pct(head_apk['methods'], head_debug['methods'])} |",
            f"| كلاسات | {head_debug['classes']:,} | {head_apk['classes']:,} | {pct(head_apk['classes'], head_debug['classes'])} |",
            "",
            "debug مش نسخة المتجر: فيه كود زيادة ومفيش shrink. الرقم ده أثر R8 + تقليص الموارد، مش أثر الـ startup profile لوحده.",
        ]

    if head_r8:
        lines += [
            "",
            "### اللي R8 شاله من release",
            "",
            f"- كلاسات اتشالت: **{head_r8['removed_classes']:,}**",
            f"- ميثودز/فيلدز اتشالوا: **{head_r8['removed_members']:,}**",
            f"- موارد unused في `resources.txt`: **{head_r8['unused_resources']:,}**",
        ]

    if head_aab:
        lines += ["", "### Startup DEX layout (`r8.json`)", "", f"الملف: `{head_aab['r8_json']}`"]
        if head_aab["startup_dexes"]:
            flagged = [d for d in head_aab["startup_dexes"] if d["startup"]]
            lines.append("")
            lines.append("| DEX | startup profile اتطبق |")
            lines.append("| --- | --- |")
            for dex in head_aab["startup_dexes"]:
                lines.append(f"| `{dex['name']}` | {'نعم' if dex['startup'] else 'لا'} |")
            if not flagged:
                lines.append("")
                lines.append("`startup: true` مش ظاهر. الـ seed profile ممكن يكون لسه ما حرّكش ترتيب الـ DEX، أو شكل `r8.json` اتغير.")
        else:
            lines.append("")
            lines.append("مفيش قائمة DEX جوه `r8.json`.")

    lines += [
        "",
        "وقت الإقلاع بالمللي ثانية مش في الجدول ده. job `startup` على محاكي API 30 بيقيس `am start -W` خمس مرات بعد `cmd package compile` (`interpret-only` مقابل `speed-profile`) وبيعلّق النتيجة تحت.",
        "",
    ]
    return "\n".join(lines)


def _parse_pair(value):
    if not value:
        return None
    a, b = value.split(",", 1)
    return int(a), int(b)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--head-apk")
    parser.add_argument("--base-apk")
    parser.add_argument("--head-aab")
    parser.add_argument("--base-aab")
    parser.add_argument("--debug-apk")
    parser.add_argument("--mapping-dir")
    parser.add_argument("--head-download", help="min,max bytes")
    parser.add_argument("--base-download", help="min,max bytes")
    parser.add_argument("--out", required=True)
    args = parser.parse_args()
    report = render(
        analyze_apk(args.head_apk),
        analyze_apk(args.base_apk),
        analyze_aab(args.head_aab),
        analyze_aab(args.base_aab),
        analyze_apk(args.debug_apk),
        r8_usage(args.mapping_dir),
        {"head": _parse_pair(args.head_download), "base": _parse_pair(args.base_download)},
    )
    Path(args.out).write_text(report)
    print(report)


if __name__ == "__main__":
    main()
