# yt-downloader-android

Java/XML Android downloader application skeleton targeting Android 4.4 / API 19.

## Current state
- URL input and validation
- Background download service placeholder
- Status/progress UI
- GitHub Actions APK build
- minSdk 19

The downloader engine is isolated in DownloadService.java so it can be integrated without coupling it to the UI.

## Build
Run the GitHub Actions workflow and download the generated APK artifact.

Only download content you have permission to download and use.
