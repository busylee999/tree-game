package com.busylee.treegame.branch;

import com.busylee.treegame.ITreeMaster;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by busylee on 14.02.15.
 */
public abstract class BranchEntity extends TiledSprite {

	public enum Side {
		Left(new int[] {1 ,0 ,0 ,0}, 0),
		Top(new int[] {0 ,1 ,0 ,0}, 1),
		Right(new int[] {0 ,0 ,1 ,0}, 2),
		Bottom(new int[] {0 ,0 ,0 ,1}, 3);

		Side(int[] sideAnchors, int index) {
			this.side = sideAnchors;
            this.index = index;
		}

		public int[] side;
        public int index;

        public static Map<Integer, Side> map = new HashMap<Integer, Side>();

        static {
            for(int i = 0; i < Side.values().length; ++i)
                map.put(Side.values()[i].index, Side.values()[i]);
        }

        public static Side valueOf(int index) {
            return map.get(index);
        }

		public int[] getV() {
			return this.side;
		}
	}

	public static final int BRANCH_WIDTH = 36;
	public static final int BRANCH_HEIGHT = 36;

	public static final int DEGREE_90 = 90;
	public static final int DEGREE_360 = 360;

	protected boolean alive = false;
	protected Side anchorSide = Side.Left;

	protected int columnNumber;
	protected int rowNumber;

	private ITreeMaster mTreeMaster;

	public BranchEntity(int columnNumber, int rowNumber, int height, ITiledTextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager, ITreeMaster treeMaster) {
		super(columnNumber * BranchEntity.BRANCH_WIDTH + BranchEntity.BRANCH_WIDTH / 2,
				height - rowNumber * BranchEntity.BRANCH_HEIGHT - BranchEntity.BRANCH_HEIGHT / 2,
				BranchEntity.BRANCH_WIDTH, BranchEntity.BRANCH_WIDTH, pTextureRegion, pVertexBufferObjectManager);
		mTreeMaster = treeMaster;
		this.columnNumber = columnNumber;
		this.rowNumber = rowNumber;
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if (pSceneTouchEvent.isActionUp())
		{
			this.setRotation((this.getRotation() + DEGREE_90) % DEGREE_360);
			updateAnchor();
			mTreeMaster.onBranchTouched();
		}
		return true;
	}

	public void updateAliveState(Side side) {
		setAliveState(hasConnection(side));
        if(alive) {
			Set<Side> disturbtionSides = getDisturbtion();
			if(disturbtionSides.contains(Side.Left))
            	updateBranch(rowNumber, columnNumber - 1, Side.Right);
			if(disturbtionSides.contains(Side.Top))
            	updateBranch(rowNumber - 1, columnNumber, Side.Bottom);
			if(disturbtionSides.contains(Side.Right))
            	updateBranch(rowNumber, columnNumber + 1, Side.Left);
			if(disturbtionSides.contains(Side.Bottom))
            	updateBranch(rowNumber + 1, columnNumber, Side.Top);
        }
    }

	protected abstract HashMap<Side, Set<Side>> getDisturtionTable();

	protected  Set<Side> getDisturbtion() {
		return getDisturtionTable().get(anchorSide);
	}

	public void setAliveState(boolean alive) {
		this.alive = alive;
		if(alive)
			setCurrentTileIndex(1);
		else
			setCurrentTileIndex(0);

	}

    protected void updateBranch(int i, int j, Side side) {
        if(i == rowNumber && j == columnNumber)
            return;

        BranchEntity branchEntity = getBranch(i, j);
        if(branchEntity != null && !branchEntity.alive)
            branchEntity.updateAliveState(side);
    }

	protected void updateAnchor() {
		switch ((int) getRotation() / DEGREE_90) {
			case 0:
				anchorSide = Side.Left;
				break;
			case 1:
				anchorSide = Side.Top;
				break;
			case 2:
				anchorSide = Side.Right;
				break;
			case 3:
				anchorSide = Side.Bottom;
				break;
		}
	}

	public boolean hasConnection(Side side) {
		return getDisturbtion().contains(side);
	}

	protected BranchEntity getBranch(int i, int j) {
		return mTreeMaster.getBranch(i, j);
	}

}