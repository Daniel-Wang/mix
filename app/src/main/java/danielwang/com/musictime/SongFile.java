package danielwang.com.musictime;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;

/**
 * Created by Daniel on 2016-11-23.
 */
public class SongFile extends ContextWrapper implements Comparable<SongFile>{
    private final String fileName;
    private final File file;
    private MediaPlayer mp;

    public SongFile(final String fileName, final File f, Context context) {
        super(context);
        this.fileName = fileName;
        this.file = f;

        Uri u = Uri.parse(file.toString());
        mp = MediaPlayer.create(context, u);
    }

    // getters

    public int getDuration(){
        return mp.getDuration();
    }

    public File getFile(){
        return file;
    }

    public MediaPlayer getMediaPlayer(){
        return mp;
    }

    @Override
    public String toString() {
        return (fileName == null ? "" : fileName);
    }

    @Override
    public int compareTo(SongFile other) {
        return Long.compare(mp.getDuration(), other.mp.getDuration());
    }
}
