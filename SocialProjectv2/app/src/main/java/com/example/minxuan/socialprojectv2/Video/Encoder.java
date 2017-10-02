package com.example.minxuan.socialprojectv2.Video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Rmo on 2017/9/28.
 */

public class Encoder {
    private final static String TYPE = "video/avc";
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    MediaCodec mediaCodec;
    int width,height,frameRate,yStride,cStride,ySize,cSize,halfWidth,halfHeight;
    int timeoutUSec = 50;
    long frameIndex = 0;
    byte[] spsPpsInfo = null,yuv420 = null;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public Encoder() {}
    /**初始化編碼器**/
    public boolean init(int width, int height, int framerate, int bitrate) {
        try {
            mediaCodec = MediaCodec.createEncoderByType(TYPE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean isSupport = false;
        int colorFormat = 0;
        MediaCodecInfo codecInfo = selectCodec(TYPE);
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(TYPE);
        for (int i = 0; i < capabilities.colorFormats.length && colorFormat == 0; i++) {
            int format = capabilities.colorFormats[i];
            if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                isSupport = true;
                break;
            }
        }
        if (!isSupport)
            return false;

        this.width = width;
        this.height = height;
        this.halfWidth = width / 2;
        this.halfHeight = height / 2;
        this.frameRate = framerate;
        this.yStride = (int) Math.ceil(width / 16.0f) * 16;
        this.cStride = (int) Math.ceil(width / 32.0f) * 16;
        this.ySize = yStride * height;
        this.cSize = cStride * height / 2;
        this.yuv420 = new byte[width * height * 3 / 2];

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(TYPE, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        return true;
    }
    /**影像編碼**/
    public byte[] data_change(byte[] input)
    {
        YVtoYUV(input, yuv420, width, height);
        try {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0)
            {
                long pts = 132 + frameIndex * 1000000 / frameRate;
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(yuv420, 0, yuv420.length);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv420.length, pts, 0);
                frameIndex++;
            }
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUSec);
            while (outputBufferIndex >= 0)
            {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                if (spsPpsInfo == null)
                {
                    ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);
                    /**保存PPS SPS**/
                    if (spsPpsBuffer.getInt() == 0x00000001)
                    {
                        spsPpsInfo = new byte[outData.length];
                        System.arraycopy(outData, 0, spsPpsInfo, 0, outData.length);
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    outputStream.write(outData);
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUSec);
            }
            byte[] ret = outputStream.toByteArray();
            /**因編碼器生成key frame時只有SPS，要自行添加PPS**/
            if (ret.length > 5 && ret[4] == 0x65) //key frame need to add sps pps
            {
                outputStream.reset();
                outputStream.write(spsPpsInfo);
                outputStream.write(ret);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        byte[] ret = outputStream.toByteArray();
        outputStream.reset();
        return ret;
    }
    /**影像格式轉換，YV12轉換成YUV420，資料可減少一半**/
    public byte[] YVtoYUV(final byte[] input, final byte[] output, final int width, final int height)
    {
        for (int i=0; i<height; i++)
            System.arraycopy(input, yStride*i, output, yStride*i, yStride);
        for (int i=0; i<halfHeight; i++)
        {
            for (int j=0; j<halfWidth; j++)
            {
                output[ySize + (i*halfWidth + j)*2] = input[ySize + cSize + i*cStride + j];
                output[ySize + (i*halfWidth + j)*2 + 1] = input[ySize + i*cStride + j];
            }
        }
        return output;
    }
    /**獲得編碼資訊**/
    private static MediaCodecInfo selectCodec(String mimeType)
    {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++)
        {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder())
            {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++)
            {
                if (types[j].equalsIgnoreCase(mimeType))
                    return codecInfo;
            }
        }
        return null;
    }
    /**編碼結束**/
    public void close()
    {
        try
        {
            mediaCodec.stop();
            mediaCodec.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
