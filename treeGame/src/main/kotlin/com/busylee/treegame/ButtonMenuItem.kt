package com.busylee.treegame

import org.andengine.entity.scene.menu.item.IMenuItem
import org.andengine.entity.sprite.ButtonSprite
import org.andengine.input.touch.TouchEvent
import org.andengine.opengl.texture.region.ITextureRegion
import org.andengine.opengl.vbo.VertexBufferObjectManager

/**
 * Created by busylee on 26.02.16.
 */
class ButtonMenuItem:
        ButtonSprite,
        IMenuItem {

    val id: Int

    constructor(id: Int,
                width: Float,
                height: Float,
                pNormalTextureRegion: ITextureRegion?,
                pVertexBufferObjectManager:
                VertexBufferObjectManager?): super(0f, 0f, pNormalTextureRegion, pVertexBufferObjectManager) {
        this.id = id
//        mWidth = width
//        mHeight = height
    }

    override fun onAreaTouched(pSceneTouchEvent: TouchEvent?, pTouchAreaLocalX: Float, pTouchAreaLocalY: Float): Boolean {
        super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY)
        return false
    }

    override fun getID(): Int {
        return id
    }

    override fun onSelected() {
        /* do nothing */
    }

    override fun onUnselected() {
        /* do nothing */
    }
}