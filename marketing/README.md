# Qurany marketing / تسويق قرآني

## Contents
- `video/`: original silent promo; new 30-second Egyptian-Arabic narrated promo; preview frames and technical checks.
- `store_listing/`: eight 1080×1920 RGB phone frames, 1024×500 RGB feature graphic, 512×512 RGBA icon, Arabic/English text and contact sheet. Only the ten numbered/size-labelled image assets are intended for upload; contact sheets are review aids.
- `real_screens/`: twelve genuine 1080×1920 ADB captures from unmodified Qurany 3.0.5 (65), master d6a91c0, Android 11 software emulator. Status-bar demo mode is enabled. Source dimensions are verified independently of preview thumbnail dimensions.
- `voiceover/`: Egyptian script, scene timings, subtitles, normalized narration master and voice provenance.
- `composition/`: editable narrated Remotion project; `silent/` contains the earlier composition.

## Reproduce the narrated video
1. Install Node.js and ffmpeg. Install the included Cairo font on your system (Linux: copy `composition/fonts/Cairo.ttf` into `~/.local/share/fonts/`, then run `fc-cache -f`). Font license: `composition/fonts/OFL.txt`.
2. In `composition/`, run `npm install`.
3. Run `npm run studio` to edit/preview, or `npm run render` to render the new picture and mux `voiceover/narration.wav` as AAC audio.
4. Replace screenshots in `real_screens/`, edit captions/timings in `composition/timings.ts`, or replace the narration master as needed. Keep scene boundaries and audio aligned.

The delivered master was rendered through the actual OpenMontage `video_compose` atelier/Remotion workflow: https://github.com/calesthio/OpenMontage . This portable project can also be rendered directly with Remotion. Remotion’s own license applies to its use. Dependencies are intentionally not vendored.

## Provenance and caveats
- New captures are from the running app, not poster crops, reconstructed UI or a fabricated database. Three highlighted verses and a three-page reading log were created through normal app use. Test/points state is demonstrative, not a claim about anyone’s real memorization.
- The original `qurany_promo.mp4` is the earlier silent version using repository poster crops. The new `qurany_promo_voice.mp4` uses the native captures.
- The voice is a generic synthetic Egyptian Arabic speaker from NAMAA Egyptian TTS (MIT model); no person’s voice was cloned. No music or Quran recitation is included in either promo’s audio. Native-speaker review is recommended. The current 3.0.5 listening UI has no exposed speed selector; the new video and listing therefore showcase actual playback/repetition rather than inventing speed controls.
- The icon is rebuilt from the app’s official launcher branding and high-resolution repository mark, with a full-square background suitable for Play masking.
- Listing copy is a draft, not a guarantee of Google Play approval. No Play Console upload or app publication is performed by this package.

## العربية
الحزمة تحتوي على فيديو جديد بتعليق مصري، ولقطات حقيقية من التطبيق، وصور متجر Google Play، ووصف مقترح بالعربية والإنجليزية، وملفات قابلة للتعديل. اللقطات الأصلية فعلًا 1080×1920؛ حجم المعاينة المصغّر في أدوات المراجعة ليس حجم الملف. يُرجى مراجعة النطق والنصوص ومطابقتها للإصدار الذي سيُنشر قبل استخدام الحزمة تجاريًا.

Sources: https://github.com/MahmoudMabrok/QuranyApp · https://github.com/calesthio/OpenMontage · https://huggingface.co/NAMAA-Space/NAMAA-Egyptian-TTS
Play image guidance: https://support.google.com/googleplay/android-developer/answer/9866151?hl=en
Icon guidance: https://developer.android.com/distribute/google-play/resources/icon-design-specifications
