package com.busylee.treegame;

import android.graphics.Typeface;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.busylee.treegame.branch.BranchType;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.align.HorizontalAlign;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.busylee.treegame.branch.BranchType.*;

/**
 * Created by busylee on 14.02.15.
 */
public class TreeGame extends SimpleBaseGameActivity implements MenuScene.IOnMenuItemClickListener, TreeMaster.ITreeMasterObserver {
    // ===========================================================
    // Constants
    // ===========================================================

    protected static final int MENU_START = 0;
    protected static final int MENU_QUIT = MENU_START + 1;
    protected static final int MENU_RESUME = MENU_START + 2;
    protected static final int MENU_LEVEL = MENU_START + 3;

    protected static final int MENU_LEVEL_1 = MENU_START + 4;
    protected static final int MENU_LEVEL_2 = MENU_START + 5;
    protected static final int MENU_LEVEL_3 = MENU_START + 6;
    protected static final int MENU_BACK = MENU_START + 7;

    protected static final int LEVEL_1 = 1;
    protected static final int LEVEL_2 = 2;
    protected static final int LEVEL_3 = 3;

    protected static final int TIMER_INTERVAL = 1000; //1000 ms
    protected static final int SCORE_LIMIT_IN_SEC = 60 * 5; // 5 min

//    private static final int mCameraWidth = 480;
//    private static final int mCameraHeight = 720;

    // ===========================================================
    // Fields
    // ===========================================================3

	private Map<BranchType, ITiledTextureRegion> mBranchTilesMap;

    protected ITextureRegion mMenuStartTextureRegion;
	protected ITextureRegion mMenuButtonTextureRegion;
    protected ITextureRegion mMenuQuitTextureRegion;

	private Camera mCamera;

	private Scene mGameScene;
    private MenuScene mMenuScene;
	private VertexBufferObjectManager mVertexBufferObjectManager;

	private TreeMaster mTreeHolder;

    private Text mScoreText;

    private int mScore;
    private boolean mTimer;

    private int mCameraWidth;
    private int mCameraHeight;

    private int mMenuWidth;
    private int mMenuHeight;

    private int level = 1;

    private Handler mHandler;
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if(mTimer) {
                mScore ++;
                mScoreText.setText(String.valueOf(mScore));
                if(mScore < SCORE_LIMIT_IN_SEC)
                    mHandler.postDelayed(this, TIMER_INTERVAL);
                else
                    onGameLose();
            }
        }
    };

	// ===========================================================
    // AndEngine lifecycle methods
    // ===========================================================


    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        mHandler = new Handler();
    }

    @Override
	protected void onCreateResources() throws IOException {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mVertexBufferObjectManager = this.getVertexBufferObjectManager();

		this.mBranchTilesMap = new HashMap<BranchType, ITiledTextureRegion>(5);
        BitmapTextureAtlas mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128, 260, TextureOptions.DEFAULT);
		mBranchTilesMap.put(Leaf, BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_leaf.png", 0, 1, 2, 1)); // 32x32
		mBranchTilesMap.put(LongBranch, BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_long.png", 0, 66, 2, 1)); // 32x32
		mBranchTilesMap.put(DoubleEnded, BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_double_ended.png", 0,131, 2, 1)); // 32x32
		mBranchTilesMap.put(TripleEnded, BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_triple_ended.png", 0, 196, 2, 1)); // 32x32
//		mBranchTilesMap.put(Root, BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_leaf.png", 0, 131, 1, 1)); // 32x32
		mBitmapTextureAtlas.load();

        BitmapTextureAtlas mMenuTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 200, 150, TextureOptions.BILINEAR);
        this.mMenuStartTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_reset.png", 0, 0);
		this.mMenuButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_button.png", 0, 50);
        this.mMenuQuitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_quit.png", 0, 100);
        mMenuTextureAtlas.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mGameScene = new Scene();
		this.mGameScene.setBackground(new Background(0, 20, 20));
		this.mTreeHolder = new TreeMaster(this.mGameScene, this.mBranchTilesMap, this.mVertexBufferObjectManager, this);

        ButtonSprite menuButton = new ButtonSprite(0 , 0, this.mMenuButtonTextureRegion, this.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                showMenu(true);
                pauseGame();
                return true;
            }
        };
        menuButton.setPosition(mCameraWidth - menuButton.getWidth() / 2, mCameraHeight - menuButton.getHeight() / 2);

        // CREATE SCORE TEXT
        Font font = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
        font.load();

        mScoreText = new Text(0, mCameraHeight, font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), this.getVertexBufferObjectManager());
        mScoreText.setPosition(0 + mScoreText.getWidth() / 2, mCameraHeight - mScoreText.getHeight() / 2);
        this.mGameScene.attachChild(mScoreText);

        this.mGameScene.registerTouchArea(menuButton);
        this.mGameScene.attachChild(menuButton);

        showMenu();
        return this.mGameScene;
	}



    @Override
    public EngineOptions onCreateEngineOptions() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mCameraWidth = metrics.widthPixels;
        mCameraHeight = metrics.heightPixels;
        mMenuWidth = mCameraWidth / 4;
        mMenuHeight = mCameraHeight / 8;
        mCamera = new Camera(0, 0, mCameraWidth, mCameraHeight);

        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,  new FillResolutionPolicy(), mCamera);
    }

    @Override
    public synchronized void onPauseGame() {
        super.onPauseGame();
        showMenu(true);
    }

    // ===========================================================
    // UI game methods (menu, scene transitions, etc.)
    // ===========================================================

    private void showMenu() {
        showMenu(false);
    }

    private void showMenu(boolean isPause) {
        this.mMenuScene = new MenuScene(this.mCamera);

        int menuButtonWidth = mMenuWidth;
        int menuButtonHeight = mMenuHeight;

        if(isPause) {
            final SpriteMenuItem continueMenuItem = new SpriteMenuItem(MENU_RESUME, menuButtonWidth, menuButtonHeight, this.mMenuStartTextureRegion, this.getVertexBufferObjectManager());
            continueMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            continueMenuItem.setPosition(mCameraWidth / 2, mCameraHeight / 2);
            this.mMenuScene.addMenuItem(continueMenuItem);
        }

        final SpriteMenuItem newGameMenuItem = new SpriteMenuItem(MENU_START, menuButtonWidth, menuButtonHeight, this.mMenuStartTextureRegion, this.getVertexBufferObjectManager());
        newGameMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        newGameMenuItem.setPosition(mCameraWidth / 2, mCameraHeight / 2);
        this.mMenuScene.addMenuItem(newGameMenuItem);

        final SpriteMenuItem levelChangeItem = new SpriteMenuItem(MENU_LEVEL, menuButtonWidth, menuButtonHeight, this.mBranchTilesMap.get(Leaf), this.getVertexBufferObjectManager());
        levelChangeItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        levelChangeItem.setPosition(mCameraWidth / 2, mCameraHeight / 2);
        this.mMenuScene.addMenuItem(levelChangeItem);

        final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, menuButtonWidth, menuButtonHeight, this.mMenuQuitTextureRegion, this.getVertexBufferObjectManager());
        quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        this.mMenuScene.addMenuItem(quitMenuItem);

        this.mMenuScene.buildAnimations();

        this.mMenuScene.setBackgroundEnabled(true);

        this.mMenuScene.setOnMenuItemClickListener(this);

        this.mGameScene.setChildScene(this.mMenuScene, false, true, true);
    }

    private void showLevelMenu() {
        MenuScene levelScene = new MenuScene(this.mCamera);

        int menuButtonWidth = mMenuWidth;
        int menuButtonHeight = mMenuHeight;

        final SpriteMenuItem buttonLevel1 = new SpriteMenuItem(MENU_LEVEL_1, menuButtonWidth, menuButtonHeight, this.mMenuStartTextureRegion, this.getVertexBufferObjectManager());
        buttonLevel1.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        buttonLevel1.setPosition(mCameraWidth / 2, mCameraHeight / 2);
        levelScene.addMenuItem(buttonLevel1);

        final SpriteMenuItem buttonLevel2 = new SpriteMenuItem(MENU_LEVEL_2, menuButtonWidth, menuButtonHeight, this.mMenuStartTextureRegion, this.getVertexBufferObjectManager());
        buttonLevel2.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        buttonLevel2.setPosition(mCameraWidth / 2, mCameraHeight / 2);
        levelScene.addMenuItem(buttonLevel2);

        final SpriteMenuItem buttonLevel3 = new SpriteMenuItem(MENU_LEVEL_3, menuButtonWidth, menuButtonHeight, this.mMenuQuitTextureRegion, this.getVertexBufferObjectManager());
        buttonLevel3.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        levelScene.addMenuItem(buttonLevel3);

        final SpriteMenuItem butonBack = new SpriteMenuItem(MENU_BACK, menuButtonWidth, menuButtonHeight, this.mMenuQuitTextureRegion, this.getVertexBufferObjectManager());
        butonBack.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        levelScene.addMenuItem(butonBack);

        levelScene.buildAnimations();

        levelScene.setBackgroundEnabled(true);

        levelScene.setOnMenuItemClickListener(this);

        this.mMenuScene.setChildScene(levelScene, false, true, true);
    }

    @Override
    public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
        switch(pMenuItem.getID()) {
            case MENU_START:
                this.mGameScene.clearChildScene();
                startGame();
                return true;
            case MENU_RESUME:
                this.mGameScene.clearChildScene();
                continueGame();
                return true;
            case MENU_LEVEL:
                showLevelMenu();
                return true;
            case MENU_QUIT:
                stopTimer();
				/* End Activity. */
                this.finish();
                return true;
            case MENU_LEVEL_1:
                this.mGameScene.clearChildScene();
                changeLevel(LEVEL_1);
                startGame();
                return true;
            case MENU_LEVEL_2:
                this.mGameScene.clearChildScene();
                changeLevel(LEVEL_2);
                startGame();
                return true;
            case MENU_LEVEL_3:
                this.mGameScene.clearChildScene();
                changeLevel(LEVEL_3);
                startGame();
                return true;
            case MENU_BACK:
                mMenuScene.clearChildScene();
                return true;
            default:
                return false;
        }
    }

    private void changeLevel(int level) {
        this.level = level;
    }

    // ===========================================================
    // Game control methods
    // ===========================================================

    private synchronized void startGame() {
        removeTree();
        showTree();
        resetTimer();
        startTimer();
    }

    private synchronized void pauseGame() {
        stopTimer();
    }

    private void continueGame() {
        startTimer();
    }

    private synchronized void onGameLose() {
        stopTimer();
        //todo stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TreeGame.this, "You lose!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

	@Override
    public void onGameWin() {
        stopTimer();
        //todo stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TreeGame.this, "You win!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer() {
        mTimer = true;
        mHandler.postDelayed(mTimerRunnable, TIMER_INTERVAL);
    }

    private void stopTimer() {
        mTimer = false;
        mHandler.removeCallbacks(mTimerRunnable);
    }

    private void resetTimer() {
        stopTimer();
        mScore = 0;
        mScoreText.setText(String.valueOf(mScore));
    }

    // ===========================================================
    // Game entity interaction methods
    // ===========================================================

	private void showTree() {

        final int scoreHeight = 40;

        final int padding = mCameraWidth / 20;

        final int ableWidth = mCameraWidth - padding * 2;
        final int ableHeigh = mCameraHeight - padding * 2;

        int branchSize = ableWidth / 4;

        if (level == 1) {
            int requiredRowCount = 3;
            int requiredColumnCount = 3;
        } else if (level == 2){

        } else if (level ==3) {

        }

		int rowCount = ( ableHeigh - scoreHeight ) / branchSize;
		int columnCount = ableWidth / branchSize;

        TreePosition treePosition = new TreePosition();
        treePosition.yFrom = rowCount * branchSize +
                ( ( mCameraHeight - scoreHeight ) - rowCount * branchSize ) / 2;
        treePosition.xFrom = (mCameraWidth - columnCount * branchSize ) / 2;

		int[][] levelMatrix = LevelMatrixGenerator.generateLevelMatrix(rowCount, columnCount);

		mTreeHolder.initTree(branchSize, treePosition, columnCount, rowCount, levelMatrix);
		mTreeHolder.shakeTree();

	}

	private void showCorrectAnswer() {
        this.mTreeHolder.showCorrectAnswer();
    }

	private void removeTree() {
		this.mTreeHolder.removeTree();
	}

}
