package edi.md.androidcash.Utils;
import android.content.Context;

/**
 * Created by Igor on 13.05.2020
 */

public class DownloadController {
    private Context context;
    private String url;

    public DownloadController(Context context, String uri) {
        this.context = context;
        this.url = uri;
    }
}
