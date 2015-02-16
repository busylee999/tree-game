package com.busylee.treegame;

import android.widget.Toast;
import com.busylee.treegame.branch.BranchEntity;
import com.busylee.treegame.branch.BranchLeaf;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import java.io.IOException;

/**
 * Created by busylee on 14.02.15.
 */
public class TreeGame extends SimpleBaseGameActivity implements ITreeMaster {

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private static BranchEntity[][] mBranchMatrix;

	private ITextureRegion
			mBranch0,
			mBranchLeafTextureRegion,
			mBranchLongTextureRegion,
			mBranchDoubleEndedTextureRegion,
			mBranch4,
			mBranch5;

	private Scene mScene;
	private VertexBufferObjectManager mVertexBufferObjectManager;

	@Override
	protected void onCreateResources() throws IOException {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mVertexBufferObjectManager = this.getVertexBufferObjectManager();

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 128, TextureOptions.BILINEAR);
		this.mBranchLeafTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "branch_leaf.png", 0, 0); // 64x32
		this.mBranchLongTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "branch_long.png", 0, 20); // 64x32
		this.mBranchDoubleEndedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "branch_double_ended.png", 0, 64, 2, 1); // 64x32
		this.mBranch4 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_hexagon_tiled.png", 0, 96, 2, 1); // 64x32
		this.mBranch5 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_hexagon_tiled.png", 0, 96, 2, 1); // 64x32
		this.mBitmapTextureAtlas.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 20, 20));

		showTree();

		return this.mScene;
	}

	private void showTree() {

		int rowCount = CAMERA_HEIGHT / BranchEntity.BRANCH_HEIGHT;
		int columnCount = CAMERA_WIDTH / BranchEntity.BRANCH_WIDTH;
		mBranchMatrix = new BranchEntity[rowCount][columnCount];

		for(int i = 0 ; i < rowCount; ++i )
			for (int j = 0; j < columnCount; ++j ) {
				mBranchMatrix[i][j] = addBranch(j % 2 == 1 ? BranchType.Leaf : BranchType.LongBranch, j , i );
			}
	}

	private BranchEntity addBranch(BranchType branchType, int columnNumber, int rowNumber) {

		ITextureRegion branchTextureRegion = null;

		switch (branchType) {
			case LongBranch:
				branchTextureRegion = this.mBranchLongTextureRegion;
				break;
			case Leaf:
				branchTextureRegion = this.mBranchLeafTextureRegion;
				break;
		}

		if(branchTextureRegion != null) {
			BranchEntity branchEntity = createBranchEntity(branchType, columnNumber, rowNumber);
			this.mScene.registerTouchArea(branchEntity);
			this.mScene.attachChild(branchEntity);
			return branchEntity;
		}

		return null;
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this, "Touch the screen to add objects.", Toast.LENGTH_LONG).show();

		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}


	private BranchEntity createBranchEntity(BranchType branchType, int columnNumber, int rowNumber) {
		BranchEntity branchEntity = null;

		switch (branchType) {

            case Root: {

            }

			case DoubleEnded:
				branchEntity = new BranchLeaf(
						columnNumber,
						rowNumber,
						mBranchLeafTextureRegion, mVertexBufferObjectManager, this);
				break;
			case Leaf:
				branchEntity = new BranchLeaf(
				columnNumber,
						rowNumber,
						mBranchLeafTextureRegion, mVertexBufferObjectManager, this);
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
		TripleEnded,
		TetraEnded
	}
}
