package com.busylee.treegame.branch;

import com.busylee.treegame.ITreeMaster;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by busylee on 14.02.15.
 */
public class BranchDoubleEnded extends BranchEntity {

	static

	public BranchDoubleEnded(int columnNumber, float rowNumber, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager, ITreeMaster treeMaster) {
		super(columnNumber, rowNumber, pTextureRegion, pVertexBufferObjectManager, treeMaster);
	}

	@Override
	public void updateAliveState(Side side, boolean aliveState) {
		switch (anchorSide) {

		}
	}
}
