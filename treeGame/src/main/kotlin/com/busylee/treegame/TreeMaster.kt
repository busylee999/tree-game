package com.busylee.treegame

import com.busylee.treegame.branch.*
import com.busylee.treegame.branch.BranchType.*
import org.andengine.entity.scene.Scene
import org.andengine.opengl.texture.region.ITiledTextureRegion
import org.andengine.opengl.vbo.VertexBufferObjectManager
import org.andengine.util.math.MathUtils
import java.util.*

/**
 * Created by busylee on 16.02.16.
 */
class TreeMaster(
        val mGameScene: Scene,
        val mSpriteTextures: Map<BranchType,ITiledTextureRegion>,
        val mVertexBufferObjectManager: VertexBufferObjectManager,
        val mTreeMasterObserver: ITreeMasterObserver) : ITreeMaster {

    var mBranchRoot: BranchEntity? = null

    var mBranchMatrix : Array<Array<BranchEntity>> = arrayOf(arrayOf())
    var mBranchCorrectAnswer : Array<Array<BranchEntity.Side>> = arrayOf(arrayOf())

    override fun getBranch(i: Int, j: Int): BranchEntity? {
        if (i >= 0 && i < mBranchMatrix.size && j >= 0 && j < mBranchMatrix[0].size)
            return mBranchMatrix[i][j]
        else
            return null
    }

    override fun onBranchTouched() {
        deathAllBranches()
        mBranchRoot?.updateAliveState(BranchEntity.Side.Left)

        if (checkWin())
            mTreeMasterObserver.onGameWin()
    }

    fun initTree(branchSize: Int, treePosition: TreePosition, columnCount: Int, rowCount: Int, levelMatrix: Array<IntArray>) {
        val vertexCount = columnCount * rowCount;

        var branchType = Root
        var branchCorrectSide: BranchEntity.Side = BranchEntity.Side.Left

        val initFunc = {
            i: Int, j: Int ->
                val vertexNumber = i * columnCount + j
                val summ = MathUtils.sum(levelMatrix[vertexNumber])

                if (summ == 1) {
                    branchType = BranchType.Leaf
                    if (vertexNumber != 0 && levelMatrix[vertexNumber][vertexNumber - 1] == 1)
                        branchCorrectSide = BranchEntity.Side.Left
                    else if (vertexNumber < vertexCount - 1 && levelMatrix[vertexNumber][vertexNumber + 1] == 1)
                        branchCorrectSide = BranchEntity.Side.Right
                    else if (i > 0 && levelMatrix[vertexNumber][vertexNumber - columnCount] == 1)
                        branchCorrectSide = BranchEntity.Side.Top
                    else if (i < rowCount - 1 && levelMatrix[vertexNumber][vertexNumber + columnCount] == 1)
                        branchCorrectSide = BranchEntity.Side.Bottom
                } else if (summ == 3) {
                    branchType = BranchType.TripleEnded
                    if (i == 0 || i > 0 && levelMatrix[vertexNumber][vertexNumber - columnCount] == 0)
                        branchCorrectSide = BranchEntity.Side.Right
                    else if (i == rowCount - 1 || i < rowCount - 1 && levelMatrix[vertexNumber][vertexNumber + columnCount] == 0)
                        branchCorrectSide = BranchEntity.Side.Left
                    else if (levelMatrix[vertexNumber][vertexNumber - 1] == 0)
                        branchCorrectSide = BranchEntity.Side.Top
                    else if (levelMatrix[vertexNumber][vertexNumber + 1] == 0)
                        branchCorrectSide = BranchEntity.Side.Bottom
                } else if (summ == 2) {
                    branchType = BranchType.DoubleEnded
                    if (vertexNumber == 0
                            || i == 0 && levelMatrix[vertexNumber][vertexNumber + 1] == 1
                            || i < rowCount - 1 && levelMatrix[vertexNumber][vertexNumber + 1] == 1 && levelMatrix[vertexNumber][vertexNumber + columnCount] == 1)
                        branchCorrectSide = BranchEntity.Side.Right
                    else if (vertexNumber == vertexCount - 1
                            || i > 0 && levelMatrix[vertexNumber][vertexNumber - 1] == 1 && levelMatrix[vertexNumber][vertexNumber - columnCount] == 1
                            || i == rowCount - 1 && levelMatrix[vertexNumber][vertexNumber - 1] == 1)
                        branchCorrectSide = BranchEntity.Side.Left
                    else if (i > 0 && levelMatrix[vertexNumber][vertexNumber + 1] == 1 && levelMatrix[vertexNumber][vertexNumber - columnCount] == 1 || i == rowCount - 1 && levelMatrix[vertexNumber][vertexNumber + 1] == 1)
                        branchCorrectSide = BranchEntity.Side.Top
                    else if (i == 0 && levelMatrix[vertexNumber][vertexNumber - 1] == 1 || i < rowCount - 1 && levelMatrix[vertexNumber][vertexNumber - 1] == 1 && levelMatrix[vertexNumber][vertexNumber + columnCount] == 1)
                        branchCorrectSide = BranchEntity.Side.Bottom

                    if (j > 0 && j + 1 < columnCount) {
                        if (levelMatrix[vertexNumber][vertexNumber - 1] == 1 && levelMatrix[vertexNumber][vertexNumber + 1] == 1) {
                            branchType = BranchType.LongBranch
                            branchCorrectSide = BranchEntity.Side.Left
                        }
                    }

                    if (i > 0 && i + 1 < rowCount) {
                        if (levelMatrix[vertexNumber - columnCount][vertexNumber] == 1 && levelMatrix[vertexNumber + columnCount][vertexNumber] == 1) {
                            branchType = BranchType.LongBranch
                            branchCorrectSide = BranchEntity.Side.Top
                        }
                    }

                }

                addBranch(branchType, j, i, treePosition, branchCorrectSide, branchSize)
//                createBranchEntity(branchType, j, i, treePosition, this)
            }

        mBranchMatrix = Array(rowCount) {
            i ->
                Array(columnCount) {
                    j ->
                        initFunc(i, j)
                }
        }

//        addBranch(Leaf, 5, 1, treePosition, BranchEntity.Side.Left)
//        addBranch(LongBranch, 5, 3, treePosition, BranchEntity.Side.Left)
//        addBranch(DoubleEnded, 5, 6, treePosition, BranchEntity.Side.Left)
//        addBranch(TripleEnded, 5, 7, treePosition, BranchEntity.Side.Left)

        mBranchCorrectAnswer = Array(rowCount) {
            i ->
                Array(columnCount) {
                    j ->
                        mBranchMatrix[i][j].anchorSide
                }
        }
    }

    fun addBranch(branchType: BranchType, columnNumber: Int, rowNumber: Int, treePosition: TreePosition, side: BranchEntity.Side, branchSize: Int): BranchEntity {
        val branchEntity = createBranchEntity(branchType, columnNumber, rowNumber, treePosition, branchSize, this)
        branchEntity.anchorSide = side
        this.mGameScene.registerTouchArea(branchEntity)
        this.mGameScene.attachChild(branchEntity)
        return branchEntity
    }

    fun shakeTree() {
        val random = Random(System.currentTimeMillis())
        for (i in mBranchMatrix.indices)
            for (j in 0..mBranchMatrix[i].size - 1)
                mBranchMatrix[i][j].setAnchorSide(BranchEntity.Side.valueOf(random.nextInt(4)))

        mBranchRoot?.updateAliveState(BranchEntity.Side.Left)
    }

    fun showCorrectAnswer() {
        for (i in mBranchMatrix.indices)
            for (j in 0..mBranchMatrix[i].size - 1)
                mBranchMatrix[i][j].anchorSide = mBranchCorrectAnswer[i][j]

        mBranchRoot?.updateAliveState(BranchEntity.Side.Left)
    }

    fun removeTree() {
        mBranchRoot = null
            for (i in mBranchMatrix.indices)
                for (j in 0..mBranchMatrix[i].size - 1) {
                    mGameScene.unregisterTouchArea(mBranchMatrix[i][j])
                    mBranchMatrix[i][j].detachSelf()
                    mBranchMatrix[i][j].dispose()
                }
    }

    fun deathAllBranches() {
        for (i in mBranchMatrix.indices)
            for (j in 0..mBranchMatrix[i].size - 1)
                mBranchMatrix[i][j].setAliveState(false)
    }

    fun checkWin(): Boolean {
        for (i in mBranchMatrix.indices)
            for (j in 0..mBranchMatrix[i].size - 1)
                if (!mBranchMatrix[i][j].isAlive)
                    return false

        return true
    }


    fun createBranchEntity(branchType: BranchType, columnNumber: Int, rowNumber: Int, treePosition: TreePosition, branchSize: Int, treeMaster: ITreeMaster): BranchEntity {
        var branchEntity: BranchEntity

        if (branchType == Root || branchType == Leaf) {
            if (mBranchRoot == null) {
                branchEntity = BranchRoot(
                        columnNumber,
                        rowNumber,
                        treePosition,
                        mSpriteTextures[Leaf], mVertexBufferObjectManager, branchSize, treeMaster)
                mBranchRoot = branchEntity
            } else {
                branchEntity = BranchLeaf(
                        columnNumber,
                        rowNumber,
                        treePosition,
                        mSpriteTextures[Leaf], mVertexBufferObjectManager, branchSize, treeMaster)
            }
        }
        else if (branchType == LongBranch) branchEntity = BranchLongEnded(
                columnNumber,
                rowNumber,
                treePosition,
                mSpriteTextures[branchType], mVertexBufferObjectManager, branchSize, treeMaster)
        else if (branchType == DoubleEnded) branchEntity = BranchDoubleEnded(
                columnNumber,
                rowNumber,
                treePosition,
                mSpriteTextures[branchType], mVertexBufferObjectManager, branchSize, treeMaster)
        else branchEntity = BranchTripleEnded(
                columnNumber,
                rowNumber,
                treePosition,
                mSpriteTextures[branchType], mVertexBufferObjectManager, branchSize, treeMaster)

        return branchEntity
    }

    interface  ITreeMasterObserver {
        fun onGameWin()
    }
}
