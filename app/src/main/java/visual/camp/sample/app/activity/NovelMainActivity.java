package visual.camp.sample.app.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import camp.visual.gazetracker.GazeTracker;
import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.filter.OneEuroFilterManager;
import camp.visual.gazetracker.gaze.GazeInfo;
import camp.visual.gazetracker.state.EyeMovementState;
import camp.visual.gazetracker.util.ViewLayoutChecker;
import visual.camp.sample.app.Book;
import visual.camp.sample.app.BookAdapter;
import visual.camp.sample.app.GazeTrackerManager;
import visual.camp.sample.app.R;
import visual.camp.sample.view.GazePathView;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class NovelMainActivity extends AppCompatActivity {
    private static final String TAG = DemoActivity.class.getSimpleName();
    private final ViewLayoutChecker viewLayoutChecker = new ViewLayoutChecker();
    private GazePathView gazePathView;
    private GazeTrackerManager gazeTrackerManager;
    private final OneEuroFilterManager oneEuroFilterManager = new OneEuroFilterManager(
            2, 30, 0.5F, 0.001F, 1.0F);

    private Spinner novelSpinner;
    private Button selectButton;

    private RecyclerView rvBookList;
    private List<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_novel_main);
        gazeTrackerManager = GazeTrackerManager.getInstance();
        Log.i(TAG, "gazeTracker version: " + GazeTracker.getVersionName());
//
//        novelSpinner = findViewById(R.id.novel_spinner);
//        selectButton = findViewById(R.id.btn_select_novel);
//
//        // Example novel list
//        final String[] novels = {"小说1", "小说2", "小说3"};
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, novels);
//        novelSpinner.setAdapter(adapter);
//
//        selectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(NovelMainActivity.this, NovelReadActivity.class);
//                intent.putExtra("selectedNovel", novels[novelSpinner.getSelectedItemPosition()]);
//                startActivity(intent);
//            }
//        });

        setContentView(R.layout.activity_novel_shelf);

        rvBookList = findViewById(R.id.rvBookList);
        books = new ArrayList<>(); // 这里可以从数据库或其他源填充电子书数据
        // 示例
        books.add(new Book("The Journey of Luna", "AI"));
        books.add(new Book("Example Book 2", "Author 2"));
        books.add(new Book("Example Book 3", "Author 3"));
        books.add(new Book("Example Book 4", "Author 4"));

        BookAdapter bookAdapter = new BookAdapter(this,books);
        rvBookList.setAdapter(bookAdapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gazeTrackerManager.startGazeTracking();
        setOffsetOfView();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        gazeTrackerManager.stopGazeTracking();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        gazeTrackerManager.removeCallbacks(gazeCallback);
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
    gazePathView = findViewById(R.id.gazePathView2);

//    AssetManager am = getResources().getAssets();
//    InputStream is = null;
//
//    try {
//      is = am.open("palace_seoul.jpg");
//      Bitmap bm = BitmapFactory.decodeStream(is);
//      ImageView catView = findViewById(R.id.catImage);
//      catView.setImageBitmap(bm);
//      is.close();
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
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

    private final GazeCallback gazeCallback = new GazeCallback() {
        @Override
        public void onGaze(GazeInfo gazeInfo) {
            if (oneEuroFilterManager.filterValues(gazeInfo.timestamp, gazeInfo.x, gazeInfo.y)) {
                float xCoordinate = gazeInfo.x;
                float yCoordinate = gazeInfo.y;
                float[] filtered = oneEuroFilterManager.getFilteredValues();
                gazePathView.onGaze(filtered[0], filtered[1], gazeInfo.eyeMovementState == EyeMovementState.FIXATION);
            }
        }
    };
}
