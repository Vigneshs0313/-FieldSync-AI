with open('/app/applet/app/src/main/res/drawable/ic_launcher_foreground.xml', 'w') as f:
    f.write('<?xml version="1.0" encoding="utf-8"?>\n')
    f.write('<layer-list xmlns:android="http://schemas.android.com/apk/res/android">\n')
    f.write('    <item\n')
    f.write('        android:width="66dp"\n')
    f.write('        android:height="66dp"\n')
    f.write('        android:drawable="@drawable/ic_fieldsync_logo"\n')
    f.write('        android:gravity="center" />\n')
    f.write('</layer-list>\n')
