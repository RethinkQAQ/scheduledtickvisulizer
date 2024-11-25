package mypals.ml;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.awt.*;
import java.util.ArrayList;

public class StringRenderer {
    public static double lastTickPosX = 0;
    public static double lastTickPosY = 0;
    public static double lastTickPosZ = 0;
    public static void renderText(MatrixStack matrixStack, RenderTickCounter counter, BlockPos pos, String text, int color, float SIZE, boolean seeThrow)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Vec3d textPos = new Vec3d(pos.toCenterPos().toVector3f());
        drawString(matrixStack, counter, camera, textPos, text, color, SIZE, seeThrow);
    }
    public static void renderText(MatrixStack matrixStack, RenderTickCounter counter, Vec3d pos, String text, int color, float SIZE, boolean seeThrow)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        drawString(matrixStack, counter, camera, pos, text, color, SIZE, seeThrow);
    }
    public static void renderTextList(MatrixStack matrixStack, BlockPos pos, float tickDelta, float line, ArrayList<String> texts, ArrayList<Integer> colors, float size) {
        drawStringList(matrixStack, pos, tickDelta,  line, texts, colors, size) ;

        }
    private static VertexConsumerProvider.Immediate getVertexConsumer() {
        return MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    }
    public static void drawStringList(MatrixStack matrixStack, BlockPos textPos, float tickDelta, float line, ArrayList<String> texts, ArrayList<Integer> colors, float size) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Matrix4fStack modelViewMatrix = new Matrix4fStack(1);
        modelViewMatrix.identity();

        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {

            float x = (float) (textPos.toCenterPos().getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
            float y = (float) (textPos.toCenterPos().getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
            float z = (float) (textPos.toCenterPos().getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));
            lastTickPosX = camera.getPos().getX();
            lastTickPosY = camera.getPos().getY();
            lastTickPosZ = camera.getPos().getZ();
            modelViewMatrix.translate(x, y, z);
            modelViewMatrix.rotate(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
            modelViewMatrix.scale(size, -size, 1);

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            RenderSystem.disableDepthTest();

            float totalHeight = 0.0F;
            for (String text : texts) {
                totalHeight += textRenderer.getWrappedLinesHeight(text, Integer.MAX_VALUE) * 1.25F;
            }

            float renderYBase = -totalHeight / 2.0F; // 起始位置，从底部开始
            for (int i = 0; i < texts.size(); i++) {
                float renderX = -textRenderer.getWidth(texts.get(i)) * 0.5F; // 居中
                float renderY = renderYBase + textRenderer.getWrappedLinesHeight(texts.get(i), Integer.MAX_VALUE) * 1.25F * i;

                // 渲染文本
                VertexConsumerProvider.Immediate immediate = getVertexConsumer();
                textRenderer.draw(
                        texts.get(i), renderX, renderY, colors.get(i) != null? colors.get(i) : Color.white.getRGB(), false, modelViewMatrix, immediate,
                        TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0
                );
                immediate.draw();
            }

            // 恢复矩阵状态
            RenderSystem.enableDepthTest();
        }
    }

    public static void drawString(MatrixStack matrixStack,RenderTickCounter tickCounter, Camera camera, Vec3d textPos, String text, int color, float SIZE, boolean seeThrow) {

        Matrix4fStack modelViewMatrix = new Matrix4fStack(1);
        modelViewMatrix.identity();

        float tickDelta = tickCounter.getTickDelta(true);
        float x = (float) (textPos.x - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (textPos.y - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (textPos.z - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));
        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();
        modelViewMatrix.translate(x, y, z);
        modelViewMatrix.rotate(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
        modelViewMatrix.scale(SIZE, -SIZE, 1);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float totalWidth = textRenderer.getWidth(text);
        float writtenWidth = 1;
        float renderX = -totalWidth * 0.5F + writtenWidth;

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.disableDepthTest();

        if(seeThrow)
            textRenderer.draw(text, renderX, 0, color, false, modelViewMatrix
                    , immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        else
            textRenderer.draw(text, renderX, 0, color, false, modelViewMatrix
                    , immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        immediate.draw();
        RenderSystem.enableDepthTest();

    }

}