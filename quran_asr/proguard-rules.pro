# Dynamic feature rules. Activities are kept via the manifest.
# ModelDownloader is called from DownloadModelActivity and, once the split is
# installed, reflectively from the base module's AsrFeatureManager.
-keep class education.mahmoud.quranyapp.asr.ModelDownloader { *; }
