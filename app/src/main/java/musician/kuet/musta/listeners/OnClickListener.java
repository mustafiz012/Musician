package musician.kuet.musta.listeners;

import musician.kuet.musta.models.Song;

/**
 * Created by musta on 2/8/18.
 */

public interface OnClickListener {
    void onClick(Song song);

    /**
     * Created by musta on 2/8/18.
     */

    interface OnItemLongClickListener {
        void onItemLongClick();
    }

    /**
     * Created by musta on 2/8/18.
     */

    interface OnItemClickListener {
        void onItemClick();
    }
}
