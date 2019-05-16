package ru.evgeniyvasilev.sibsutis.bugs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public enum Status {
        ALIVE,
        DEAD
    };

    Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Integer[] position = {0};
        timer = new Timer();
        final Point point = new Point();
        this.getWindow().getWindowManager().getDefaultDisplay().getSize(point);

        final List<Personage> personages = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            personages.add(new Personage(point, this));
        }

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    personages.get(i).animate();
                }
            }
        };

        timer.schedule(timerTask, 25, 25);
    }

    public class Position
    {
        private int x;
        private int y;
        private int angle;
        Position(int x, int y, int angle)
        {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getAngle() {
            return angle;
        }
    }

    private class MovingPattern
    {
        private int tick = 0;

        public Position move()
        {
            tick++;
            return action();
        }

        protected Position action() {
            return new Position(0, 0, 0);
        }

        public MovingPattern notifyEdge(Edges edge) {
            if (edge == Edges.TOP) {
                reachedTopEdge();
            } else if (edge == Edges.RIGHT) {
                reachedRightEdge();
            } else if (edge == Edges.BOTTOM) {
                reachedBottomEdge();
            } else if (edge == Edges.LEFT) {
                reachedLeftEdge();
            }

            return this;
        }

        protected void reachedTopEdge() {
            throw new UnsupportedOperationException();
        }

        protected void reachedBottomEdge() {
            throw new UnsupportedOperationException();
        }

        protected void reachedRightEdge() {
            throw new UnsupportedOperationException();
        }

        protected void reachedLeftEdge() {
            throw new UnsupportedOperationException();
        }
    }

    private class SimpleMovingPattern extends MovingPattern
    {
        protected int directionX = 1;
        protected int directionY = 1;
        protected int angle = 135;

        @Override
        protected Position action() {
            return new Position(directionX, directionY, angle);
        }

        private void calculateAngle() {
            if (directionX > 0 && directionY > 0) {
                angle = 135;
            } else if (directionX > 0 && directionY < 0) {
                angle = 45;
            } else if (directionX < 0 && directionY > 0) {
                angle = 210;
            } else {
                angle = 315;
            }
        }

        @Override
        public MovingPattern notifyEdge(Edges edge) {
            return super.notifyEdge(edge);
        }

        @Override
        protected void reachedTopEdge() {
            directionY *= -1;
            calculateAngle();
        }

        @Override
        protected void reachedRightEdge() {
            directionX *= -1;
            calculateAngle();
        }

        @Override
        protected void reachedBottomEdge() {
            directionY *= -1;
            calculateAngle();
        }

        @Override
        protected void reachedLeftEdge() {
            directionX *= -1;
            calculateAngle();
        }
    }

    public enum Edges {TOP, BOTTOM, RIGHT, LEFT};

    private class Personage
    {

        private ImageView imageView;
        private Integer originalWidth;
        private Integer originalHeight;
        private Float movingAngle;
        private Float renderedAngle;
        private Status status;
        private int directionX = 1;
        private int directionY = 1;

        private float positionX;
        private float positionY;
        private int speed;

        private Point screenDimensions;
        private MovingPattern movingPattern;

        Personage(Point screenDimensions, Context context) {
            this.screenDimensions = screenDimensions;
            initImage(context);

            this.positionX = (int) ((Math.random() * 100000) % (this.screenDimensions.x - originalWidth));
            this.positionY = (int) ((Math.random() * 100000) % (this.screenDimensions.y - originalHeight));
            movingPattern = new SimpleMovingPattern();

            status = Status.ALIVE;
            speed = (int) (Math.random() * 10) % 10;
            this.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status = Status.DEAD;
                    v.setVisibility(View.INVISIBLE);
                }
            });
        }

        public Personage setSpeed(int speed)
        {
            this.speed = speed;
            return this;
        }

        protected void initImage(Context context) {
            imageView = new ImageView(context);
            imageView.setImageResource(R.mipmap.ic_launcher_round);
            RelativeLayout relativeLayout = findViewById(R.id.layout);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            BitmapFactory.Options dimensions = new BitmapFactory.Options();
            dimensions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round, dimensions);
            originalHeight = dimensions.outHeight;
            originalWidth =  dimensions.outWidth;
//            screenDimensions.x = relativeLayout.getWidth();
//            screenDimensions.y = relativeLayout.getHeight();

            relativeLayout.addView(imageView, layoutParams);
        }

        void animate() {
            float newX = positionX;
            float newY = positionY;


            Position newSpeed = movingPattern.move();
            newX = newX + newSpeed.getX() * directionX * speed;
            newY = newY + newSpeed.getY() * directionY * speed;
            renderedAngle = (float) newSpeed.getAngle();

            boolean reachedEdge = false;
            if (newY < 0) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.TOP);
            }

            if (newX > screenDimensions.x - originalWidth) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.RIGHT);
            }

            if (newY > screenDimensions.y - originalHeight) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.BOTTOM);
            }

            if (newX < 0) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.LEFT);
            }

            if (reachedEdge) {
                newSpeed = movingPattern.move();
                newX = newX + newSpeed.getX() * directionX * speed;
                newY = newY + newSpeed.getY() * directionY * speed;
                renderedAngle = (float) newSpeed.getAngle();
            }

            this.positionX = newX;
            this.positionY = newY;
            imageView.setX((int) newX);
            imageView.setY((int) newY);
            imageView.setRotation(renderedAngle);
        }
    }
}
