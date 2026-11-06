# خطة تقريب زخارف المصحف للنصّي · Closing the decoration gap (text mushaf)

**Branch:** `feature/use-custom-mushaf` · **PR:** [#158](https://github.com/MahmoudMabrok/QuranyApp/pull/158)  
**App dep:** `com.github.MahmoudMabrok:mushaf-text-customizable:56ebe3d`  
**Fork:** [MahmoudMabrok/mushaf-text-customizable](https://github.com/MahmoudMabrok/mushaf-text-customizable) (of [sherifshabans/mushaf-text](https://github.com/sherifshabans/mushaf-text))  
**Classic reader decorations:** `app/.../showSuraAyas/mushaf/` (`MushafFrameDrawable`, `SuraHeaderSpan`, `MushafPalette`, `MushafModels`) · wired from `PageViewHolder`  
**Text reader wiring:** `mushaftext/MushafView.kt` → `MushafPage(..., decoration = …)`

---

## 1. ملخص الأسباب الجذرية · Root causes (summary)

The fork already ports **parts** of the classic look (`MushafDecoration`, `drawOrnatePageFrame`, `drawClassicSurahHeader`). It still cannot match the classic reader because of structural limits, not only missing paint calls:

1. **No layout-aware decoration hooks.** Classic `MushafFrameDrawable` asks `hizbCenterY: () -> Float?` so the hizb medallion sits on the line where a quarter starts (`PageViewHolder` + `HizbMark`). The library Canvas draw has page geometry but **no API to report per-line Y**, and `drawOrnatePageFrame` takes only `pageNumber` — no hizb.
2. **Fixed Madinah line budget.** `layout.txt` + reserved `Banner`/`Basmala` slots (`MushafLayout.kt`, `buildLineSlots` in `MushafPage.kt`) keep **exactly** 15 printed lines. Classic `SuraHeaderSpan` is a `ReplacementSpan` inside flowing TextView text: it **grows the line height** and can carry rich meta (order, revelation, ayah count). The library banner is one reserved line; rich side panels do not fit without stealing verse space or changing the printed layout contract.
3. **Incomplete ports vs View Canvas.** Classic cartouche uses cubic `Path` + scale stroke + side **text** panels (`SuraHeaderSpan.drawClassic`). Fork `drawClassicSurahHeader` uses a **rounded rect** cartouche and draws **empty** side panel strokes; `ayahCount` on `DrawnBanner` is unused. Page medallion is a **circle without** Arabic page digits / 8-point rosette; hizb medallion is missing entirely.
4. **Chrome lives outside the page in the app.** Classic juz title (`JuzNames.of`) and sura name sit in `PageViewHolder` header views; the frame owns the page-number medallion. `MushafView` adds a plain centered footer `BasicText(toArabicDigits(page))` **and** (when framed) an empty medallion — double / wrong chrome. No juz bar in `MushafView`.
5. **Different glyph systems for ayah ends.** Library: KFGQPC digits → rosette ligature (`MushafFont`). Classic: `NumberHelper` + me_quran ornate parentheses. Matching “our” markers would mean a different encoding or a custom end-mark drawer — not a theme flag.
6. **Theming is shallow.** `MushafColors` / `MushafFrameColors` cover paper/ink/frame, but classic `MushafPalette` ties header+frame+night to one style enum and custom green/yellow paper from settings. No slot for app fonts (`MushafFonts`) or full palette injection.
7. **No Compose slots for app-owned overlays.** Everything decorative is internal `DrawScope` in `MushafPageBody`. The app cannot inject “draw hizb here” or “use our SuraHeaderPainter” without forking again. Needed: metrics callback and/or `decorationPainter` / content slots with insets.

---

## 2. زخرفة بزخرفة · Per decoration

### 2.1 إطار الصفحة · Page frame / border

| | |
|---|---|
| **App today** | `MushafFrameDrawable.kt`: EVEN_ODD band, thin diamonds, outer rope of beads, double rules, corner diamonds, **8-point rosette** page medallion with Arabic digits, mirrored thick band for odd/even pages; `contentPadding()` insets the TextView. |
| **Library** | `MushafFrameDrawing.kt` `drawOrnatePageFrame` (~L23–128): same band/rope idea; medallion is a plain circle **without text**; **no hizb**. `MushafPage` adds fixed `framePad` when `decoration.pageFrame` (≈L177–186). |
| **Gap** | Missing rosette path + digit text + hizb; padding is hardcoded dp, not shared with drawable units; no odd/even awareness beyond margins. |
| **Fork change** | Finish port of `rosette` + `drawText` for page number; accept optional `HizbDecoration?` + `hizbCenterY`; expose `frameInsets(page): PaddingValues` from the same geometry. |
| **App change** | Stop drawing footer page number when `pageFrame`; pass hizb from DB/`HizbMark.find`. |
| **Effort** | M · **Risk** | Low if tests pin frame PNGs. |

### 2.2 رأس السورة · Sura header styles

| | |
|---|---|
| **App today** | `SuraHeaderSpan` + `SuraHeaderInfo`: CLASSIC cartouche (cubic path) with **name + orderLabel**; side panels **نزولها / آياتها**; MODERN pill with revelation + count; PLAIN text. Height scales with text size. |
| **Library** | Reserved `LineSlot.Banner`; `drawClassicSurahHeader` / modern pill / plain (`MushafPage.kt` ~L569–580, `MushafFrameDrawing.kt` ~L133+). Name only via `AnnotatedString("سُورَةُ " + …)`. Side panels stroked empty; `DrawnBanner.ayahCount` unused. |
| **Gap** | No revelation/order/count text; cartouche shape simplified; one reserved line cannot hold classic header height without overlapping or shrinking verses. |
| **Fork change** | (A) Rich `SurahHeaderInfo` in decoration API and draw full classic/modern text; and/or (B) `headerContent: @Composable (SurahHeaderScope) -> Unit` / painter callback with box bounds. Optionally allow banner to use 1–2 layout lines or overlay above pitch. |
| **App change** | Map `SuraHeaderInfo` from Room/assets; prefer injecting app painter once slots exist to avoid duplicating paths. |
| **Effort** | L · **Risk** | Medium — layout contract vs visual fidelity. |

### 2.3 البسملة · Basmala

| | |
|---|---|
| **App today** | Part of page TextView / mushaf text flow (same Quran font); no separate ornate basmala drawable beyond text. |
| **Library** | `LineSlot.Basmala` measures `BASMALA` centered (`MushafPage.kt` ~L707–716). |
| **Gap** | Mostly OK; optional larger/ornamented basmala would need a style flag or custom slot. |
| **Fork / app** | Optional `basmalaStyle`; low priority. |
| **Effort** | S · **Risk** | Low. |

### 2.4 علامات الآيات · Ayah number markers

| | |
|---|---|
| **App today** | `NumberHelper` + me_quran-style ornate parentheses around Arabic digits. |
| **Library** | End marks as digit tokens shaped by **KFGQPC** into a single rosette glyph (`MushafTheme` / README encoding notes). |
| **Gap** | Different typeface/encoding by design; “same decorations” for markers means either accept KFGQPC look or supply `endMarkDrawer`. |
| **Fork change** | Optional `AyahEndMarkStyle` / custom drawer measuring like current tokens so layout tests still pass. |
| **App change** | Only if product insists on me_quran markers inside text mushaf. |
| **Effort** | L · **Risk** | High (layout/tajweed measurement). Prefer accept library markers. |

### 2.5 ألوان الخلفية · Background / paper colours

| | |
|---|---|
| **App today** | Night + green/yellow/white via `backColorState` / `MushafPalette.of(...)`. |
| **Library** | `MushafColors.Light/Dark`; app already passes `paperColor` into `colors.copy(paper = …)` in `MushafView`. Frame/header colours from `MushafFrameColors.of(style)` — **not** fully tied to green/yellow paper. |
| **Gap** | Frame/header palette ignores custom paper; night frame colours may need Dark-specific `MushafFrameColors`. |
| **Fork change** | `MushafFrameColors.from(paper, night, style)` or accept full `MushafFrameColors` override on `MushafDecoration`. |
| **App change** | Map settings colours → override. |
| **Effort** | S–M · **Risk** | Low. |

### 2.6 علامات الجزء والحزب · Juz / hizb markers

| | |
|---|---|
| **App today** | Header `tvJuz` + `JuzNames`; `HizbMark` medallion on outer band at line Y (`MushafFrameDrawable` + `Page.hizbMark`). |
| **Library** | `MushafPager` has simple surah/juz chrome (not used by our `MushafView`). Frame API: **no hizb**. No juz chrome in `MushafView`. |
| **Gap** | Missing data plumbing + Y metrics + drawing. |
| **Fork change** | Publish `PageLayoutMetrics` (line tops/bottoms, content rect); `decoration.hizb`; or app overlay using metrics. |
| **App change** | Compute `HizbMark` / juz from existing DB; draw overlay or pass into decoration. |
| **Effort** | M · **Risk** | Medium (Y sync with font-fit). |

### 2.7 رأس وتذييل الصفحة · Page header / footer

| | |
|---|---|
| **App today** | Sura name + juz in top bar; page number in frame medallion when framed. |
| **Library / MushafView** | Extra footer `BasicText` always; with frame, empty circle also drawn → inconsistent. |
| **Gap** | Chrome ownership unclear. |
| **Fork change** | Option `showPageNumberInFrame`; metrics for medallion center. |
| **App change** | `MushafView` header row (juz + sura) like classic; suppress duplicate footer when framed. |
| **Effort** | S · **Risk** | Low. |

---

## 3. اتجاه التصميم المقترح في الفورك · Proposed fork API direction

Prefer **metrics + painters** over copying every path forever:

```kotlin
data class MushafPageMetrics(
  val page: Int,
  val contentBounds: Rect,
  val lineBands: List<Rect>, // 1..lineCount
  val bannerBounds: List<Pair<Int /*surah*/, Rect>>,
  val pageNumberAnchor: Offset?,
)

fun interface MushafDecorationPainter {
  fun DrawScope.paint(metrics: MushafPageMetrics, colors: MushafColors)
}

data class MushafDecoration(
  val pageFrame: Boolean = false,
  val surahHeaderStyle: SurahHeaderStyle = CLASSIC,
  val ayahFrame: Boolean = false,
  val frameColors: MushafFrameColors? = null,
  val surahHeader: SurahHeaderModel? = null, // name, order, revelation, ayahCount
  val hizb: HizbDecoration? = null,
  val painter: MushafDecorationPainter? = null, // app escape hatch
)
```

Keep built-in painters as defaults; let QuranyApp pass a painter that reuses logic ported from `MushafFrameDrawable` / `SuraHeaderSpan` until parity is good enough to delete duplication.

---

## 4. ترتيب العمل · Order of work

| # | Work | Where | Effort |
|---|------|-------|--------|
| 1 | Suppress duplicate page number; add juz/sura chrome in `MushafView` | App | S |
| 2 | Page medallion text + proper rosette; Dark/custom `MushafFrameColors` | Fork → bump JitPack hash in app | M |
| 3 | Expose `MushafPageMetrics` (line bands, content insets) | Fork | M |
| 4 | Hizb medallion via metrics + app `HizbMark` | Fork + app | M |
| 5 | Full surah header text (order / revelation / count) + real cartouche path | Fork (or app painter) | L |
| 6 | Optional `decorationPainter` / header slot | Fork | M |
| 7 | Decide ayah end-mark policy (keep KFGQPC vs custom drawer) | Product + fork | L |
| 8 | Screenshot / render harness parity tests (classic vs text) | App CI / fork tests | M |

---

## 5. مخاطر · Risks

- **Printed layout purity:** enlarging banners or overlays can collide with the 15-line guarantee and tajweed hit-testing.
- **Dual maintenance:** porting paths into the fork **and** keeping View drawables doubles bugs; prefer one painter implementation shared or generated.
- **JitPack pin churn:** app already pins commit hashes (`56ebe3d`); each fork fix needs a deliberate bump + emulator check.
- **Compose vs View fonts:** classic uses `MushafFonts` typefaces; Compose needs `FontFamily` assets — pack once in app or fork.
- **Odd/even mirroring:** must stay consistent with RTL pager index (`page = index + 1`).

---

## 6. ما هو “جيد بما يكفي” · Definition of done (visual)

Text mushaf with `pageFrame=true` and CLASSIC header should, on a sample of pages (1, 49–50, 100, 604):

1. Show ornate frame with readable page medallion (no second footer number).
2. Show juz + sura chrome comparable to classic top bar.
3. Show hizb medallion when a quarter starts on the page.
4. Show sura header with name + at least ayah-count and revelation (or order).
5. Honour night mode and settings paper colour on paper **and** frame/header palette.
6. Keep long-press / highlights / read log behaviour unchanged.

Ayah end marks may remain KFGQPC unless explicitly required otherwise.

---

## 7. حالة الشغل الحالي · Current state on this branch

Already done toward decorations (historical commits on `feature/use-custom-mushaf`):

- Depend on customizable fork + `MushafDecoration` wiring.
- App overlays / library ports for frame padding, ornate frame, improved surah header, `drawText` fixes.
- Settings: frame + header controls shown for **both** full and custom mushaf modes (`4c2fde87`).

This document records **why parity is still incomplete** and the ordered work to finish it.
