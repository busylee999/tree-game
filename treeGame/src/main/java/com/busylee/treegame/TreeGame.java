package com.busylee.treegame;

import android.graphics.Typeface;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.busylee.treegame.branch.BranchDoubleEnded;
import com.busylee.treegame.branch.BranchEntity;
import com.busylee.treegame.branch.BranchLeaf;
import com.busylee.treegame.branch.BranchLongEnded;
import com.busylee.treegame.branch.BranchRoot;
import com.busylee.treegame.branch.BranchTripleEnded;

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
import org.andengine.util.math.MathUtils;

import java.io.IOException;
import java.util.Random;

/**
 * Created by busylee on 14.02.15.
 */
public class TreeGame extends SimpleBaseGameActivity implements ITreeMaster, MenuScene.IOnMenuItemClickListener {
    // ===========================================================
    // Constants
    // ===========================================================

    protected static final int MENU_START = 0;
    protected static final int MENU_QUIT = MENU_START + 1;
    protected static final int MENU_CONTINUE = MENU_START + 2;

    protected static final int TIMER_INTERVAL = 1000; //1000 ms
    protected static final int SCORE_LIMIT_IN_SEC = 60 * 5; // 5 min

    private static final int CAMERA_WIDTH = 480;
    private static final int CAMERA_HEIGHT = 720;

    // ===========================================================
    // Fields
    // ===========================================================3

    private BranchEntity[][] mBranchMatrix;
	private BranchEntity.Side[][] mBranchCorrectAnswer;

	private ITiledTextureRegion
			mBranchRootTextureRegion,
			mBranchLeafTextureRegion,
			mBranchLongTextureRegion,
			mBranchDoubleEndedTextureRegion,
			mBranchTripleEndedTextureRegion;

    protected ITextureRegion mMenuStartTextureRegion;
    protected ITextureRegion mMenuQuitTextureRegion;

	private Scene mGameScene;
    private MenuScene mMenuScene;

	private VertexBufferObjectManager mVertexBufferObjectManager;

	private BranchRoot mBranchRoot = null;
    private Camera mCamera;

    private Text mScoreText;

    private int mScore;
    private boolean mTimer;

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

        BitmapTextureAtlas mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 160, TextureOptions.BILINEAR);
		this.mBranchLeafTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_leaf.png", 0, 0, 2, 1); // 32x32
		this.mBranchLongTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_long.png", 0, 32, 2, 1); // 32x32
		this.mBranchDoubleEndedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_double_ended.png", 0, 64, 2, 1); // 32x32
		this.mBranchTripleEndedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_triple_ended.png", 0, 96, 2, 1); // 32x32
		this.mBranchRootTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "branch_root.png", 0, 128, 1, 1); // 32x32
		mBitmapTextureAtlas.load();

        BitmapTextureAtlas mMenuTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
        this.mMenuStartTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_reset.png", 0, 0);
        this.mMenuQuitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_quit.png", 0, 50);
        mMenuTextureAtlas.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mGameScene = new Scene();
		this.mGameScene.setBackground(new Background(0, 20, 20));

        ButtonSprite pauseButton = new ButtonSprite(0 , 0, this.mMenuStartTextureRegion, this.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                showMenu(true);
                pauseGame();
                return true;
            }
        };
        pauseButton.setPosition(CAMERA_WIDTH - pauseButton.getWidth() / 2, CAMERA_HEIGHT - pauseButton.getHeight() / 2);

        // CREATE SCORE TEXT
        Font font = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
        font.load();

        mScoreText = new Text(0, CAMERA_HEIGHT, font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), this.getVertexBufferObjectManager());
        mScoreText.setPosition(0 + mScoreText.getWidth() / 2, CAMERA_HEIGHT - mScoreText.getHeight() / 2);
        this.mGameScene.attachChild(mScoreText);

        this.mGameScene.registerTouchArea(pauseButton);
        this.mGameScene.attachChild(pauseButton);

        showMenu();
        return this.mGameScene;
	}



    @Override
    public EngineOptions onCreateEngineOptions() {
        mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,  new FillResolutionPolicy(), mCamera);
    }

    @Override
    public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
        switch(pMenuItem.getID()) {
            case MENU_START:
                this.mGameScene.clearChildScene();
                startGame();
                return true;
            case MENU_CONTINUE:
                this.mGameScene.clearChildScene();
                continueGame();
                return true;
            case MENU_QUIT:
                stopTimer();
				/* End Activity. */
                this.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public synchronized void onPauseGame() {
        super.onPauseGame();
//        showMenu(true);
    }

    // ===========================================================
    // UI game methods (menu, scene transitions, etc.)
    // ===========================================================

    private void showMenu() {
        showMenu(false);
    }

    private void showMenu(boolean isPause) {
        this.mMenuScene = new MenuScene(this.mCamera);

        if(isPause) {
            final SpriteMenuItem continueMenuItem = new SpriteMenuItem(MENU_CONTINUE, this.mMenuStartTextureRegion, this.getVertexBufferObjectManager());
            continueMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            continueMenuItem.setPosition(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
            this.mMenuScene.addMenuItem(continueMenuItem);
        }

        final SpriteMenuItem newGameMenuItem = new SpriteMenuItem(MENU_START, this.mMenuStartTextureRegion, this.getVertexBufferObjectManager());
        newGameMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        newGameMenuItem.setPosition(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
        this.mMenuScene.addMenuItem(newGameMenuItem);

        final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, this.mMenuQuitTextureRegion, this.getVertexBufferObjectManager());
        quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        this.mMenuScene.addMenuItem(quitMenuItem);

        this.mMenuScene.buildAnimations();

        this.mMenuScene.setBackgroundEnabled(false);

        this.mMenuScene.setOnMenuItemClickListener(this);

        this.mGameScene.setChildScene(this.mMenuScene, false, true, true);
    }

    // ===========================================================
    // Game control methods
    // ===========================================================

    private synchronized void startGame() {
        mBranchRoot = null;
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

    private void onGameWin() {
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

		int rowCount = ( CAMERA_HEIGHT - scoreHeight ) / BranchEntity.BRANCH_HEIGHT;
		int columnCount = CAMERA_WIDTH / BranchEntity.BRANCH_WIDTH;

        TreePosition treePosition = new TreePosition();
        treePosition.yFrom = rowCount * BranchEntity.BRANCH_HEIGHT +
                ( ( CAMERA_HEIGHT - scoreHeight ) - rowCount * BranchEntity.BRANCH_HEIGHT ) / 2;
        treePosition.xFrom = (CAMERA_WIDTH - columnCount * BranchEntity.BRANCH_WIDTH ) / 2;

		int vertexCount = rowCount * columnCount;

		mBranchMatrix = new BranchEntity[rowCount][columnCount];
		mBranchCorrectAnswer = new BranchEntity.Side[rowCount][columnCount];

        int[][] levelMatrix = LevelMatrixGenerator.generateLevelMatrix(rowCount, columnCount);

		BranchType branchType = BranchType.Root;
		BranchEntity.Side branchCorrectSide = BranchEntity.Side.Left;

		for(int i = 0 ; i < rowCount; ++i )
			for (int j = 0 ; j < columnCount ; ++j ) {
				int vertexNumber = i * columnCount + j;
				int summ = MathUtils.sum(levelMatrix[vertexNumber]);

				if(summ == 1) {
					branchType = BranchType.Leaf;
					if(vertexNumber != 0 && levelMatrix[vertexNumber][vertexNumber - 1] == 1)
						branchCorrectSide = BranchEntity.Side.Left;
					else if( vertexNumber < vertexCount - 1 && levelMatrix[vertexNumber][vertexNumber + 1] == 1)
						branchCorrectSide = BranchEntity.Side.Right;
					else if(i > 0 && levelMatrix[vertexNumber][vertexNumber - columnCount] == 1)
						branchCorrectSide = BranchEntity.Side.Top;
					else if(i < rowCount - 1 && levelMatrix[vertexNumber][vertexNumber + columnCount] == 1)
						branchCorrectSide = BranchEntity.Side.Bottom;
				}
				else if(summ == 3) {
					branchType = BranchType.TripleEnded;
					if(i == 0 || i > 0 && levelMatrix[vertexNumber][vertexNumber - columnCount] == 0)
						branchCorrectSide = BranchEntity.Side.Right;
					else if(i == rowCount - 1 || i < rowCount - 1 && levelMatrix[vertexNumber][vertexNumber + columnCount] == 0)
						branchCorrectSide = BranchEntity.Side.Left;
					else if(levelMatrix[vertexNumber][vertexNumber - 1] == 0)
						branchCorrectSide = BranchEntity.Side.Top;
					else if(levelMatrix[vertexNumber][vertexNumber + 1] == 0)
						branchCorrectSide = BranchEntity.Side.Bottom;
				}
				else if (summ == 2) {
					branchType = BranchType.DoubleEnded;
					if(vertexNumber == 0
							|| ( i==0 && levelMatrix[vertexNumber][vertexNumber + 1] == 1)
							|| ( i < rowCount - 1 && levelMatrix[vertexNumber][vertexNumber + 1] == 1 && levelMatrix[vertexNumber][vertexNumber + columnCount] == 1))
						branchCorrectSide = BranchEntity.Side.Right;
					else if(vertexNumber == vertexCount - 1
							|| ( i > 0 && levelMatrix[vertexNumber][vertexNumber - 1] == 1 && levelMatrix[vertexNumber][vertexNumber - columnCount] == 1)
							|| ( i == rowCount - 1 && levelMatrix[vertexNumber][vertexNumber - 1] == 1))
						branchCorrectSide = BranchEntity.Side.Left;
					else if ( (i > 0 && levelMatrix[vertexNumber][vertexNumber + 1] == 1 && levelMatrix[vertexNumber][vertexNumber - columnCount] == 1)
							|| ( i == rowCount - 1 && levelMatrix[vertexNumber][vertexNumber + 1] == 1))
						branchCorrectSide = BranchEntity.Side.Top;
					else if (( i==0 && levelMatrix[vertexNumber][vertexNumber - 1] == 1)
							|| ( i < rowCount - 1 && levelMatrix[vertexNumber][vertexNumber - 1] == 1 && levelMatrix[vertexNumber][vertexNumber + columnCount] == 1))
						branchCorrectSide = BranchEntity.Side.Bottom;

					if(j > 0 && j + 1  < columnCount) {
						if (levelMatrix[vertexNumber][vertexNumber - 1] == 1 && levelMatrix[vertexNumber][vertexNumber + 1] == 1) {
							branchType = BranchType.LongBranch;
							branchCorrectSide = BranchEntity.Side.Left;
						}
					}

					if(i > 0 && i + 1 < rowCount) {
						if (levelMatrix[vertexNumber - columnCount][vertexNumber] == 1 && levelMatrix[vertexNumber + columnCount][vertexNumber] == 1) {
							branchType = BranchType.LongBranch;
							branchCorrectSide = BranchEntity.Side.Top;
						}
					}

				}

				mBranchMatrix[i][j] = addBranch(branchType, j , i, treePosition);
				mBranchCorrectAnswer[i][j] = branchCorrectSide;
			}

		shakeTree(rowCount, columnCount);

	}

	private void shakeTree(int rowCount, int columnCount) {
        final Random random = new Random(System.currentTimeMillis());
		for(int i = 0; i < rowCount; ++i)
			for(int j = 0; j < columnCount; ++j)
				mBranchMatrix[i][j].setAnchorSide(BranchEntity.Side.valueOf(random.nextInt(4)));

        mBranchRoot.updateAliveState(BranchEntity.Side.Left);
	}

	private void showCorrectAnswer(int rowCount, int columnCount) {
        for (int i = 0; i < rowCount; ++i)
            for (int j = 0; j < columnCount; ++j)
                mBranchMatrix[i][j].setAnchorSide(mBranchCorrectAnswer[i][j]);

        mBranchRoot.updateAliveState(BranchEntity.Side.Left);
    }

	private BranchEntity addBranch(BranchType branchType, int columnNumber, int rowNumber, TreePosition treePosition) {
		BranchEntity branchEntity = createBranchEntity(branchType, columnNumber, rowNumber, treePosition);
		this.mGameScene.registerTouchArea(branchEntity);
		this.mGameScene.attachChild(branchEntity);
		return branchEntity;
	}

	@Override
	public void onBranchTouched() {
		deathAllBranches();
		mBranchRoot.updateAliveState(BranchEntity.Side.Left);

		if(checkWin())
			onGameWin();

	}

	private void removeTree() {
        if(mBranchMatrix != null)
            for(int i = 0 ; i < mBranchMatrix.length; ++i)
                for (int j = 0; j < mBranchMatrix[i].length; ++j)
                    if(mBranchMatrix[i][j] != null) {
                        mGameScene.unregisterTouchArea(mBranchMatrix[i][j]);
                        mBranchMatrix[i][j].detachSelf();
                        mBranchMatrix[i][j].dispose();
                    }
	}

	private void deathAllBranches() {
		for(int i = 0 ; i < mBranchMatrix.length; ++i)
			for (int j = 0; j < mBranchMatrix[i].length; ++j)
				mBranchMatrix[i][j].setAliveState(false);
	}

	private boolean checkWin() {
		for(int i = 0 ; i < mBranchMatrix.length; ++i)
			for (int j = 0; j < mBranchMatrix[i].length; ++j)
				if(!mBranchMatrix[i][j].isAlive())
					return false;

		return true;
	}

	private BranchEntity createBranchEntity(BranchType branchType, int columnNumber, int rowNumber, TreePosition treePosition) {
		BranchEntity branchEntity = null;

		switch (branchType) {
            case Root:
			case Leaf:
				if(mBranchRoot == null) {
					mBranchRoot = new BranchRoot(
							columnNumber,
							rowNumber,
                            treePosition,
							mBranchRootTextureRegion, mVertexBufferObjectManager, this);
					branchEntity = mBranchRoot;
					break;
				}

				branchEntity = new BranchLeaf(
						columnNumber,
						rowNumber,
                        treePosition,
						mBranchLeafTextureRegion, mVertexBufferObjectManager, this);
				break;
			case LongBranch:
				branchEntity = new BranchLongEnded(
						columnNumber,
						rowNumber,
                        treePosition,
						mBranchLongTextureRegion, mVertexBufferObjectManager, this);
				break;
			case DoubleEnded:
				branchEntity = new BranchDoubleEnded(
						columnNumber,
						rowNumber,
                        treePosition,
						mBranchDoubleEndedTextureRegion, mVertexBufferObjectManager, this);
				break;
			case TripleEnded:
				branchEntity = new BranchTripleEnded(
						columnNumber,
						rowNumber,
                        treePosition,
						mBranchTripleEndedTextureRegion, mVertexBufferObjectManager, this);
				break;


		}

		return branchEntity;
	}

	@Override
	public BranchEntity getBranch(int i, int j) {
		if(i >= 0 && i < mBranchMatrix.length && j >= 0 && j < mBranchMatrix[0].length)
			return mBranchMatrix[i][j];
		else return null;
	}

    enum BranchType {
		Root,
		Leaf,
		LongBranch,
		DoubleEnded,
		TripleEnded
	}
}
