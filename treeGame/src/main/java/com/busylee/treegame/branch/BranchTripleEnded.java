package com.busylee.treegame.branch;

import com.busylee.treegame.ITreeMaster;
import com.busylee.treegame.TreePosition;

import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by busylee on 16.02.15.
 */
public class BranchTripleEnded extends BranchEntity {

	private static final HashMap<Side, Set<Side>> branchVariants = new HashMap<Side, Set<Side>>();

	static {
		//branch variants
		Set<Side> connectSides = new HashSet<Side>();
		connectSides.add(Side.Left);
		connectSides.add(Side.Top);
		connectSides.add(Side.Right);
		branchVariants.put(Side.Left, connectSides);

		connectSides = new HashSet<Side>();
		connectSides.add(Side.Top);
		connectSides.add(Side.Right);
		connectSides.add(Side.Bottom);
		branchVariants.put(Side.Top, connectSides);

		connectSides = new HashSet<Side>();
		connectSides.add(Side.Right);
		connectSides.add(Side.Bottom);
		connectSides.add(Side.Left);
		branchVariants.put(Side.Right, connectSides);

		connectSides = new HashSet<Side>();
		connectSides.add(Side.Bottom);
		connectSides.add(Side.Left);
		connectSides.add(Side.Top);
		branchVariants.put(Side.Bottom, connectSides);
	}

	public BranchTripleEnded(int columnNumber, int rowNumber, TreePosition treePosition, ITiledTextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager, int branchSize, ITreeMaster treeMaster) {
        super(columnNumber, rowNumber, treePosition, pTextureRegion, pVertexBufferObjectManager, branchSize, treeMaster);
    }

	@Override
	protected HashMap<Side, Set<Side>> getDisturtionTable() {
		return branchVariants;
	}

}
