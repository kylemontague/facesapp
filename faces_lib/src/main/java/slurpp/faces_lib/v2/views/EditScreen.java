package slurpp.faces_lib.v2.views;

/**
 * Created by Kyle Montague on 22/02/2018.
 */

public interface EditScreen extends Screen {

    void resetErrors();
    void showProgress();
    void showSuccess();
    void showFailed();

}
