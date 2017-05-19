package apps.junkuvo.alertapptowalksafely;

import android.animation.LayoutTransition;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MorphAnimation {
    private final LinearLayout parentView;
    private View buttonContainer;
    private ViewGroup viewsContainer;

    private boolean isPressed;
    private int initialWidth;
    private int initialGravity;

    public boolean isPressed() {
        return isPressed;
    }

    public MorphAnimation(View buttonContainer, LinearLayout parentView, ViewGroup viewsContainer) {
        this.buttonContainer = buttonContainer;
        this.parentView = parentView;
        this.viewsContainer = viewsContainer;

        LayoutTransition layoutTransition = parentView.getLayoutTransition();
        layoutTransition.setDuration(400);
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        isPressed = false;

    }

    public void morphIntoForm() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) buttonContainer.getLayoutParams();

        initialWidth = layoutParams.width;
        initialGravity = layoutParams.gravity;

        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT;
        buttonContainer.setLayoutParams(layoutParams);

        for (int i = 1; i < viewsContainer.getChildCount(); i++) {
            viewsContainer.getChildAt(i).setVisibility(View.VISIBLE);
        }

        isPressed = true;
    }

    public void morphIntoButton() {
        for (int i = 1; i < viewsContainer.getChildCount(); i++) {
            viewsContainer.getChildAt(i).setVisibility(View.GONE);
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) buttonContainer.getLayoutParams();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = initialWidth;
        buttonContainer.setLayoutParams(layoutParams);

        isPressed = false;
    }
}
