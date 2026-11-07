#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
npx remotion render index.tsx QuranyPromoVoice ../video/.qurany_voiced_picture.mp4 --codec h264 --crf 18 --concurrency 3
ffmpeg -y -i ../video/.qurany_voiced_picture.mp4 -i ../voiceover/narration.wav -map 0:v:0 -map 1:a:0 -c:v copy -c:a aac -b:a 192k -af apad -shortest -movflags +faststart ../video/qurany_promo_voice.mp4
rm ../video/.qurany_voiced_picture.mp4
