#!/bin/sh

set -e

INPUT="rtmp://mediamtx:1935/live/${STREAM_KEY}"
OUTPUT="/var/www/hls/${STREAM_ID}"

mkdir -p "$OUTPUT"
rm -rf "$OUTPUT"/*

echo "Starting transcoder"
echo "Input: $INPUT"
echo "Output: $OUTPUT"

ffmpeg \
    -fflags +genpts \
    -i "$INPUT" \
    -map 0:v -map 0:a \
    -c:v copy \
    -c:a copy \
    -f hls \
    -hls_time 2 \
    -hls_list_size 6 \
    -hls_flags delete_segments+independent_segments \
    -hls_segment_filename "$OUTPUT/segment_%03d.ts" \
    "$OUTPUT/master.m3u8"

echo "Transcoder stopped for stream: $STREAM_ID"