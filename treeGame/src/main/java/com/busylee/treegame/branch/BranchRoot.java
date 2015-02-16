package com.busylee.treegame.branch;

import com.busylee.treegame.ITreeMaster;

import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by busylee on 15.02.15.
 */
public class BranchRoot extends BranchEntity {

    public BranchRoot(int columnNumber, float rowNumber, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager, ITreeMaster treeMaster) {
        super(columnNumber, rowNumber, pTextureRegion, pVertexBufferObjectManager, treeMaster);
        v = new int[]{1,1,1,1};
    }
}
