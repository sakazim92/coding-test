package com.coding.codingtest;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class Swiper extends ItemTouchHelper.SimpleCallback {

    private RecyclerView recyclerView;
    private mAdapter adapter;
    private List<UnderlayButton> buttons;
    private GestureDetector gestureDetector;
    private int swipedPos = -1;
    private Map<Integer, List<UnderlayButton>> buttonsBuffer;
    private Queue<Integer> recoverQueue;
    Context context;

    private VelocityTracker mVelocityTracker;

    private static final int SWIPE_THRESH = 200;
    private static final int SWIPE_VELOCITY_THRESH = 4500;

    private static  float veloX = 0;
    //static Boolean touch_event_end=false;
    static Boolean partial_swipe=false;
    private int current_index=-1;
    private static float DX=0.0f;

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        //implementing method to listen when bomb button is tapped for any row
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float tapX=e.getX();
            Log.d("TapX",String.valueOf(e.getX()));
            if(current_index>=0 && tapX>900.0){

                adapter.dismissRow(current_index);//deleted the current row from list
            }else{
                partial_swipe=true;
            }
            return true;
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent e) {

            View childView=recyclerView.findChildViewUnder(e.getX(),e.getY());
            int currPos=recyclerView.getChildAdapterPosition(childView);
            Boolean result=false;
            partial_swipe=false;
            if(e.getAction() == MotionEvent.ACTION_DOWN){
                if (mVelocityTracker == null)
                    mVelocityTracker = VelocityTracker.obtain();//calculating swiping speed
                else
                    mVelocityTracker.clear();

                mVelocityTracker.addMovement(e);
                result= true;
            }

            if(e.getAction() == MotionEvent.ACTION_MOVE)
            {
                mVelocityTracker.addMovement(e);
                mVelocityTracker.computeCurrentVelocity(1000,recyclerView.getMaxFlingVelocity());//calculating velocity in pixels per second units
                result= false;
            }

            if(e.getAction() == MotionEvent.ACTION_UP){
                veloX = mVelocityTracker.getXVelocity();//keeping track of velocity for each swipe
                result= false;
            }

            if(recoverQueue.size()>0){
                gestureDetector.onTouchEvent(e);
                if(swipedPos >=0)
                {
                    recoverQueue.add(swipedPos);
                    swipedPos = -1;
                }
                recoverSwipedItem();//if new row is touched or swiped previous row is restored if not deleted
            }

            if (swipedPos >=0){
                Point point = new Point((int) e.getRawX(), (int) e.getRawY());
                RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
                View swipedItem = swipedViewHolder.itemView;
                Rect rect = new Rect();
                swipedItem.getGlobalVisibleRect(rect);

                if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP ||e.getAction() == MotionEvent.ACTION_MOVE) {
                    if (rect.top < point.y && rect.bottom > point.y){
                        current_index=currPos;
                        gestureDetector.onTouchEvent(e);
                    }
                    else {
                        recoverQueue.add(swipedPos);
                        swipedPos = -1;
                        recoverSwipedItem();//if new row is touched or swiped previous row is restored if not deleted
                    }
                }
            }

            return result;
        }
    };

    public Swiper(Context context, RecyclerView recyclerView,mAdapter adapter) {
        super(0, ItemTouchHelper.LEFT);
        this.context=context;
        this.recyclerView = recyclerView;
        this.adapter=adapter;
        this.buttons = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);//initializing gesture detector with gesture listener to listen for single tap in bomb button
        this.recyclerView.setOnTouchListener(onTouchListener); //attaching touch listener to recyclerview to listion for (DOWN, MOVE, UP, CANCEL) motion events to calculate swipe speed for each row
        buttonsBuffer = new HashMap<>();//initialize hashMap to keep track of bomb button showing or not for each row
        recoverQueue = new LinkedList<Integer>(){
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };
        attachSwipe();//attacing the swiper object to recyclerview
    }

    //event used for drag and drop functionality
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        partial_swipe=false;
        return true;
    }

    //method called when swipe action has finished for a row
    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();

        if (swipedPos != pos)
        {
            recoverQueue.add(swipedPos);//keeping track of previous row touched or swiped
        }
        swipedPos = pos;

        if (buttonsBuffer.containsKey(swipedPos))
            buttons = buttonsBuffer.get(swipedPos);
        else
            buttons.clear();

        buttonsBuffer.clear();
        recoverSwipedItem();//if new row is touched or swiped previous row is restored if not deleted

        if(!partial_swipe && Math.abs(DX)>0.0 && veloX<0.0)
            adapter.dismissRow(viewHolder.getAdapterPosition());//delete the swiped row if not swiped till anchor point (partial swipe is false)
    }

    //implemented method to animate swipe gestures
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        View foreground=((mHolder)viewHolder).viewFront;//fetching only the foregrund view (viewFRONT) so background with bomb button remains intact
        float translationX = dX;
        View itemView = viewHolder.itemView;
        if (pos < 0){
            swipedPos = pos;
            return;
        }
        DX=dX;//keeping track of last swipe displacement in X direction

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            if(dX < 0) {
                getDefaultUIUtil().onDraw(c,recyclerView,foreground,dX,dY,actionState,isCurrentlyActive);
                partial_swipe=false;
                if(Math.abs(dX)>SWIPE_THRESH){//if current displacement is greater than the anchor point (SWIPE_THRESH)
                    if(Math.abs(veloX)<SWIPE_VELOCITY_THRESH)//if swiping velocity veloX is less than the threshold velocity (4500)
                    {
                        List<UnderlayButton> buffer = new ArrayList<>();

                        if (!buttonsBuffer.containsKey(pos)){
                            instantiateUnderlayButton(viewHolder, buffer);
                            buttonsBuffer.put(pos, buffer);
                        }
                        else {
                            buffer = buttonsBuffer.get(pos);
                        }
                        translationX = dX * buffer.size() * SWIPE_THRESH / itemView.getWidth();//calculation for anchor point gives -200
                        Log.d("translation",String.valueOf(translationX));
                        getDefaultUIUtil().onDraw(c,recyclerView,foreground,translationX,dY,actionState,isCurrentlyActive);//animation/drawing the foreground view till the anchor point
                        partial_swipe=true;

                    }
                    else{
                        if(veloX>0){//if swipe is from left to right veloX should be positive
                            getDefaultUIUtil().onDraw(c,recyclerView,foreground,0,dY,actionState,isCurrentlyActive);//animation/drawing the foreground view to its initial position
                            partial_swipe=false;
                        }
                        else{
                            getDefaultUIUtil().onDraw(c,recyclerView,foreground,dX,dY,actionState,isCurrentlyActive);//animation/drawing the foreground view parallel to original swipe gesture
                            partial_swipe=false;
                        }
                    }
                }
                else{//if swipe gesture is less than equal to anchor point
                    getDefaultUIUtil().onDraw(c,recyclerView,foreground,0,dY,actionState,isCurrentlyActive);//animation/drawing the foreground view to its initial position
                    partial_swipe=false;
                }
            }

        }
    }

    //helps in animating previous row to initial position if not deleted
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        View foreground=((mHolder)viewHolder).viewFront;
        getDefaultUIUtil().clearView(foreground);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if(viewHolder!=null){
            View foreground=((mHolder)viewHolder).viewFront;
            getDefaultUIUtil().onSelected(foreground);
        }
    }

    private synchronized void recoverSwipedItem(){
        Log.d("calling recover","recover");
        while (!recoverQueue.isEmpty()){
            int pos = recoverQueue.poll();
            if (pos > -1) {
                recyclerView.getAdapter().notifyItemChanged(pos);//recovers previously swiped or touched item to initial position if not deleted
            }
        }
    }

    public void attachSwipe(){
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);//attaches itemtouchhelper to recyclerview to listen and animate views on swipe gestures
    }

    public abstract void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons);

    //class to keep track of bomb button for each row
    public static class UnderlayButton {
        private String name;
        Context context;

        public UnderlayButton(String text,Context ctx) {
            this.name = text;
            this.context=ctx;
        }
    }

}
