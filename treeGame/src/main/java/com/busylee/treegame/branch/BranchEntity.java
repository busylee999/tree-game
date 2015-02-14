package com.busylee.treegame.branch;

import com.busylee.treegame.ITreeMaster;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by busylee on 14.02.15.
 */
public abstract class BranchEntity extends Sprite {

	public enum Side {
		Left(new int[] {1 ,0 ,0 ,0}),
		Top(new int[] {0 ,1 ,0 ,0}),
		Right(new int[] {0 ,0 ,1 ,0}),
		Bottom(new int[] {0 ,0 ,0 ,1});

		Side(int[] sideAnchors) {
			side = sideAnchors;
		}

		public int[] side;
	}

	public static final int BRANCH_WIDTH = 36;
	public static final int BRANCH_HEIGHT = 36;

	private static final int DEGREE_90 = 90;
	private static final int DEGREE_360 = 360;

	protected boolean alive = false;
	protected Side anchorSide = Side.Left;

	protected int columnNumber;
	protected int rowNumber;

	private ITreeMaster mTreeMaster;

	public BranchEntity(int columnNumber, float rowNumber, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager, ITreeMaster treeMaster) {
		super(columnNumber * BranchEntity.BRANCH_WIDTH + BranchEntity.BRANCH_WIDTH / 2,
				rowNumber * BranchEntity.BRANCH_HEIGHT + BranchEntity.BRANCH_HEIGHT / 2,
				BranchEntity.BRANCH_WIDTH, BranchEntity.BRANCH_WIDTH, pTextureRegion, pVertexBufferObjectManager);
		mTreeMaster = treeMaster;
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if (pSceneTouchEvent.isActionUp())
		{
			this.setRotation((this.getRotation() + DEGREE_90) % DEGREE_360);
			updateAnchor();
		}
		return true;
	}

	public abstract void updateAliveState(Side side);

	protected updateAround() {

	}

//	protected boolean updateAliveState(BranchEntity branchEntity, Side side) {
//		alive = branchEntity.isLifeDistributed(side);
//		return alive;
//	}
//
	private void updateAnchor() {
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
//
//	protected abstract void updateAliveStateLeft();
//	protected abstract void updateAliveStateTop();
//	protected abstract void updateAliveStateRight();
//	protected abstract void updateAliveStateBottom();
//
//	public boolean isLifeDistributed(Side side) {
//		switch (side) {
//			case Left:
//				return isLifeDistributedLeft();
//			case Top:
//				return isLifeDistributedTop();
//			case Right:
//				return isLifeDistributedRight();
//			case Bottom:
//				return isLifeDistributedBottom();
//		}
//
//		return false;
//	}
//
//	protected boolean isLifeDistributedLeft() {
//		return false;
//	}
//
//	protected boolean isLifeDistributedTop() {
//		return false;
//	}
//
//	protected boolean isLifeDistributedRight() {
//		return false;
//	}
//
//	protected boolean isLifeDistributedBottom() {
//		return false;
//	}

	public static boolean hasConnection(int [] arr1, int [] arr2) {

		for(int i = 0; i < arr1.length; ++i)
			if(arr1[i] * arr2[i] == 1)
				return true;

		return false;
	}

	protected BranchEntity getBranch(int i, int j) {
		return mTreeMaster.getBranch(i, j);
	}

}