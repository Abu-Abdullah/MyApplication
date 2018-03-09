# MyApplication
An example of android gstreamer that can take snapshot while pipeline is running.

I need to troubleshoot the function gst_native_snapshot() in:
https://github.com/Abu-Abdullah/MyApplication/blob/master/app/src/main/jni/player.c

the function is to generate snapshot from gstreamer pipeline but it gives:
E/GLib: gst_app_sink_try_pull_sample: assertion 'GST_IS_APP_SINK (appsink)' failed

the client pipeline is
"udpsrc port=5009 ! application/x-rtp,payload=96,media=video,clock-rate=90000,encoding-name=H264,a-framerate=40,width=640,height=480 ! rtph264depay ! avdec_h264 ! videoconvert ! glimagesink name=mysink"

and the server is rpi:
raspivid -n -vf -hf -fl -t 0 -g 5 -ih -ex sports -w 640 -h 480 -b 0 -fps 40 -md 0 -sh 0 -co 0 -br 50 -sa 0 -rot 180 -ss 0 -cd H264 -awb auto -qp 0 -pf high -o - | gst-launch-1.0 fdsrc ! h264parse ! rtph264pay pt=96 config-interval=10 ! multiudpsink clients=192.168.1.21:5009



reference thread:
https://lists.freedesktop.org/archives/gstreamer-android/2018-March/001192.html
