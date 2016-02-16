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

    lateinit var mBranchMatrix : Array<Array<BranchEntity>>
    lateinit var mBranchCorrectAnswer : Array<Array<BranchEntity.Side>>

    override fun getBranch(i: Int, j: Int): BranchEntity? {
        if (i >= 0 && i < mBranchMatrix.size && j >= 0 && j < mBranchMatrix[0].size)
            return mBranchMatrix[i][j]
        else
            return null
    }

    override fun onBranchTouched() {
        deathAllBranches()
        mBranchRoot!!.updateAliveState(BranchEntity.Side.Left)

        if (checkWin())
            mTreeMasterObserver.onGameWin()
    }

    fun initTree(treePosition: TreePosition, columnCount: Int, rowCount: Int, levelMatrix: Array<IntArray>) {
        val vertexCount = columnCount * rowCount;

        var branchType = Root
        var branchCorrectSide: BranchEntity.Side = BranchEntity.Side.Left

        for (i in 0..rowCount - 1)
            for (j in 0..columnCount - 1) {
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

                mBranchMatrix[i][j] = addBranch(branchType, j, i, treePosition)
                mBranchCorrectAnswer[i][j] = branchCorrectSide
            }
    }

    fun addBranch(branchType: BranchType, columnNumber: Int, rowNumber: Int, treePosition: TreePosition): BranchEntity {
        val branchEntity = createBranchEntity(branchType, columnNumber, rowNumber, treePosition, this)
        this.mGameScene.registerTouchArea(branchEntity)
        this.mGameScene.attachChild(branchEntity)
        return branchEntity
    }

    fun shakeTree() {
        val random = Random(System.currentTimeMillis())
        for (i in mBranchMatrix.indices)
            for (j in 0..mBranchMatrix[i].size - 1)
                mBranchMatrix[i][j].setAnchorSide(BranchEntity.Side.valueOf(random.nextInt(4)))

        mBranchRoot!!.updateAliveState(BranchEntity.Side.Left)
    }

    fun showCorrectAnswer() {
        for (i in mBranchMatrix.indices)
            for (j in 0..mBranchMatrix[i].size - 1)
                mBranchMatrix[i][j].setAnchorSide(mBranchCorrectAnswer[i][j])

        mBranchRoot!!.updateAliveState(BranchEntity.Side.Left)
    }

    fun removeTree() {
        mBranchRoot = null
        if (mBranchMatrix != null)
            for (i in mBranchMatrix.indices)
                for (j in 0..mBranchMatrix[i].size - 1)
                    if (mBranchMatrix[i][j] != null) {
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


    fun createBranchEntity(branchType: BranchType, columnNumber: Int, rowNumber: Int, treePosition: TreePosition, treeMaster: ITreeMaster): BranchEntity {
        var branchEntity: BranchEntity? = null

        when (branchType) {
            Root, Leaf -> {
                if (mBranchRoot == null) {
                    mBranchRoot = BranchRoot(
                            columnNumber,
                            rowNumber,
                            treePosition,
                            mSpriteTextures[branchType], mVertexBufferObjectManager, treeMaster)
                    branchEntity = mBranchRoot
                }


            }
            LongBranch -> branchEntity = BranchLongEnded(
                    columnNumber,
                    rowNumber,
                    treePosition,
                    mSpriteTextures.get(branchType), mVertexBufferObjectManager, treeMaster)
            DoubleEnded -> branchEntity = BranchDoubleEnded(
                    columnNumber,
                    rowNumber,
                    treePosition,
                    mSpriteTextures.get(branchType), mVertexBufferObjectManager, treeMaster)
            TripleEnded -> branchEntity = BranchTripleEnded(
                    columnNumber,
                    rowNumber,
                    treePosition,
                    mSpriteTextures.get(branchType), mVertexBufferObjectManager, treeMaster)
        }

        return branchEntity!!
    }

    interface  ITreeMasterObserver {
        fun onGameWin()
    }
}
