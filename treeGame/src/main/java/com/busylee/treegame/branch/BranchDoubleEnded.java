package com.busylee.treegame.branch;

import com.busylee.treegame.ITreeMaster;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by busylee on 14.02.15.
 */
public class BranchDoubleEnded extends BranchEntity {

	private static final int[][] variants = new int[][]{
            {1, 0, 1, 0},
            {0, 1, 0, 1},
    };

	public BranchDoubleEnded(int columnNumber, float rowNumber, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager, ITreeMaster treeMaster) {
		super(columnNumber, rowNumber, pTextureRegion, pVertexBufferObjectManager, treeMaster);
	}

    @Override
    public void updateAliveState(Side side) {
        alive = hasConnection(side.side, v);
        if(alive) {
            if(anchorSide == Side.Left || anchorSide == Side.Right) {

            }

        }
    }

    @Override
    protected void updateAnchor() {
        super.updateAnchor();
        switch ((int) getRotation() / DEGREE_90) {
            case 0:
                v = variants[0];
                break;
            case 1:
                v = variants[1];
                break;
            case 2:
                v = variants[0];
                break;
            case 3:
                v = variants[1];
                break;
        }
    }
}
