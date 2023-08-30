package visual.camp.sample.app.activity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.filter.OneEuroFilterManager;
import camp.visual.gazetracker.gaze.GazeInfo;
import camp.visual.gazetracker.state.EyeMovementState;
import camp.visual.gazetracker.util.ViewLayoutChecker;
import visual.camp.sample.app.GazeTrackerManager;
import visual.camp.sample.app.R;
import visual.camp.sample.view.GazePathView;

public class NovelReadActivity extends AppCompatActivity {

//    private TextView novelContent;

    private TextView tvContent, tvPageNumber;
    private Button btnPrev, btnNext;
    private List<String> pages;
    private int currentPage = 0;

    private final ViewLayoutChecker viewLayoutChecker = new ViewLayoutChecker();
    private GazePathView gazePathView;
    private GazeTrackerManager gazeTrackerManager;
    private final OneEuroFilterManager oneEuroFilterManager = new OneEuroFilterManager(
            2, 30, 0.5F, 0.001F, 1.0F);
    int btnPrevXStart , btnPrevXEnd, btnPrevYStart, btnPrevYEnd;
    int btnNextXStart, btnNextXEnd, btnNextYStart, btnNextYEnd;
    float xCoordinate, yCoordinate;
    long dwellStartTime = 0;

    Button btnSwitchMode;
    private static final int DWELL_TIME_MODE = 1;
    private static final int GAZE_GESTURE_MODE = 2;
    private int currentMode = DWELL_TIME_MODE; // 默认为dwell time模式
    int DWELL_THRESHOLD = 1000; // 1秒
    private static final float GESTURE_THRESHOLD = 80.0f; // 根据需要调整这个值
    private float previousX, previousY;
    int screenWidth, screenHeight;
    private long lastGestureTime = 0;
    private static final long COOLDOWN_TIME = 2000;  //例如2秒冷却时间

    TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_read);

//        novelContent = findViewById(R.id.novel_content);

//        String selectedNovel = getIntent().getStringExtra("selectedNovel");
        String bookName = getIntent().getStringExtra("BOOK_NAME");
        // Here, for simplicity, we just display the name of the novel as its content.
        // In a real-world scenario, you'd load the actual content of the novel.
//        novelContent.setText("1");

        name = findViewById(R.id.bookName);
        name.setText(bookName);
        gazeTrackerManager = GazeTrackerManager.getInstance();

        tvContent = findViewById(R.id.tvContent);
        tvPageNumber = findViewById(R.id.tvPageNumber);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);


        // 从txt文件读取内容并分割为页
        String fileName = bookName + ".txt";
        String content = readFromAsset(fileName);
        pages = splitTextIntoPages(content, 100);

        btnSwitchMode = findViewById(R.id.btnSwitchMode);
        btnSwitchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMode == DWELL_TIME_MODE) {
                    currentMode = GAZE_GESTURE_MODE;
                    Toast.makeText(getApplicationContext(), "Switched to Gaze Gesture Mode", Toast.LENGTH_SHORT).show();
                } else {
                    currentMode = DWELL_TIME_MODE;
                    Toast.makeText(getApplicationContext(), "Switched to Dwell Time Mode", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 设置按钮监听
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 0) {
                    currentPage--;
                    updatePage();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < pages.size() - 1) {
                    currentPage++;
                    updatePage();
                }
            }
        });

        // 显示第一页
        updatePage();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }

    private void updatePage() {
        tvContent.setText(pages.get(currentPage));
        tvPageNumber.setText(String.format("%d/%d", currentPage + 1, pages.size()));
    }

    private String readFromAsset(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString().trim();
    }
//    public List<String> splitTextIntoPages(String content, int maxCharsPerPage) {
//        List<String> pages = new ArrayList<>();
//        int length = content.length();
//        int start = 0;
//
//        while (start < length) {
//            int end = Math.min(start + maxCharsPerPage, length);
//
//            if ( content.charAt(end) == '\n') {
//                pages.add(content.substring(start, end));
//                start = end + 1;
//            } else if (start + maxCharsPerPage >= length) {
//                pages.add(content.substring(start, end));
//            }else {
//                end++;
//            }
//        }
//
//        if (pages.isEmpty()) {
//            throw new IllegalArgumentException("Text cannot be split into pages.");
//        }
//        return pages;
//    }
    private List<String> splitTextIntoPages(String text, int MAX_WORDS_PER_PAGE) {
        List<String> pages = new ArrayList<>();
        String[] paragraphs = text.split("\n");
        StringBuilder currentPage = new StringBuilder();
        int currentPageWordCount = 0;
        int consecutiveEmptyParagraphs = 0;

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                consecutiveEmptyParagraphs += 1;
            } else {
                consecutiveEmptyParagraphs = 0;
            }

            String[] words = paragraph.split(" ");

            if (consecutiveEmptyParagraphs == 2) {
                if (currentPageWordCount > 0) {
                    pages.add(currentPage.toString().trim());
                    currentPage.setLength(0);
                    currentPageWordCount = 0;
                }
                consecutiveEmptyParagraphs = 0;
                continue;
            }

            if (currentPageWordCount + words.length > MAX_WORDS_PER_PAGE &&
                    (paragraph.endsWith(".") || paragraph.endsWith("?") || paragraph.endsWith("!"))) {
                pages.add(currentPage.toString().trim());
                currentPage.setLength(0);
                currentPageWordCount = 0;
            }

            for (String word : words) {
                currentPage.append(word).append(" ");
                currentPageWordCount += 1;
            }

            currentPage.append("\n");
        }

        // Add the last page if it has content
        if (currentPageWordCount > 0) {
            pages.add(currentPage.toString().trim());
        }

        if (pages.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be split into pages.");
        }

        return pages;
    }

    @Override
    protected void onStart() {
        super.onStart();
        gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
        initView();
//        onEyeGazeChanged(xCoordinate,yCoordinate);
        System.out.println(xCoordinate);
        System.out.println(yCoordinate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gazeTrackerManager.startGazeTracking();
        setOffsetOfView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gazeTrackerManager.stopGazeTracking();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gazeTrackerManager.removeCallbacks(gazeCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {

        gazePathView = findViewById(R.id.gazePathView3);

        System.out.println("button data");
//        int btnPrevXStart = btnPrev.getLeft();
//        int btnPrevXEnd = btnPrev.getRight();
//        int btnPrevYStart = btnPrev.getTop();
//        int btnPrevYEnd = btnPrev.getBottom();
//
//        int[] location = new int[2];
//        btnPrev.getLocationOnScreen(location);
//        System.out.println(location[0]);
//        System.out.println(location[1]);
//        System.out.println(btnPrevXEnd);
//        System.out.println(btnPrevYStart);
//        System.out.println(btnPrevYEnd);

        btnPrev.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 移除监听器以确保只调用一次
                btnPrev.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                btnPrevXStart = btnPrev.getLeft();
                btnPrevXEnd = btnPrev.getRight();
                btnPrevYStart = btnPrev.getTop();
                btnPrevYEnd = btnPrev.getBottom();

//                Log.d("Coordinates", "Left: " + left + ", Right: " + right + ", Top: " + top + ", Bottom: " + bottom);
            }
        });

        btnNext.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 移除监听器以确保只调用一次
                btnNext.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                btnNextXStart = btnNext.getLeft();
                btnNextXEnd = btnNext.getRight();
                btnNextYStart = btnNext.getTop();
                btnNextYEnd = btnNext.getBottom();

            }
        });
    }

    private void setOffsetOfView() {
        System.out.println("here");
        if(gazePathView != null){
            System.out.println("here1");
        }
        viewLayoutChecker.setOverlayView(gazePathView, new ViewLayoutChecker.ViewLayoutListener() {
            @Override
            public void getOffset(int x, int y) {
                gazePathView.setOffset(x, y);
            }
        });
    }

    private boolean isInsideButtonBounds(float x, float y, View button) {
        return x >= button.getLeft() && x <= button.getRight() && y >= button.getTop() && y <= button.getBottom();
    }

    private final GazeCallback gazeCallback = new GazeCallback() {
        @Override
        public void onGaze(GazeInfo gazeInfo) {
            if (oneEuroFilterManager.filterValues(gazeInfo.timestamp, gazeInfo.x, gazeInfo.y)) {
                xCoordinate = gazeInfo.x;
                yCoordinate = gazeInfo.y;
                float[] filtered = oneEuroFilterManager.getFilteredValues();
                gazePathView.onGaze(filtered[0], filtered[1], gazeInfo.eyeMovementState == EyeMovementState.FIXATION);
                switch (currentMode){
                    case DWELL_TIME_MODE:
                        onEyeGazeChangedDT(xCoordinate,yCoordinate,btnPrev);
                        onEyeGazeChangedDT(xCoordinate,yCoordinate,btnNext);
                        break;

                    case GAZE_GESTURE_MODE:
                        onEyeGazeChangedGG(xCoordinate,yCoordinate);
                }

            }
        }
    };

        public void onEyeGazeChangedDT(float xCoordinate, float yCoordinate, View button) {
            if (isInsideButtonBounds(xCoordinate, yCoordinate, button)) {
                if (dwellStartTime == 0) {
                    dwellStartTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - dwellStartTime > DWELL_THRESHOLD) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.performClick();
                        }
                    });
                    dwellStartTime = 0; // 重置dwellStartTime
                }
            }
    }

    public void onEyeGazeChangedGG(float xCoordinate, float yCoordinate) {
        if (System.currentTimeMillis() - lastGestureTime < COOLDOWN_TIME) {
            // 如果还在冷却时间内，直接返回，不处理手势
            return;
        }else {
            if (previousX != 0 && previousY != 0) { // 确保我们有之前的坐标来比较

                float deltaX = xCoordinate - previousX;
                float deltaY = yCoordinate - previousY;

                if (deltaX < -GESTURE_THRESHOLD && deltaY < -GESTURE_THRESHOLD &&
                        previousX > screenWidth  * 0.6 &&
                        previousY > screenHeight  * 0.6) {
                    // 视线从右下角到左上角
                    // 下一页
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnNext.performClick();
                            lastGestureTime = System.currentTimeMillis();
                        }
                    });
                } else if (deltaX > GESTURE_THRESHOLD && deltaY < -GESTURE_THRESHOLD &&
                        previousX < screenWidth  * 0.4 &&
                        previousY > screenHeight  * 0.6) {
                    // 视线从左下角到右上角
                    // 上一页
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnPrev.performClick();
                            lastGestureTime = System.currentTimeMillis();
                        }
                    });
                }
            }
        }

        previousX = xCoordinate;
        previousY = yCoordinate;
    }
}

