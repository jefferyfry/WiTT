package com.clearwire.tools.wimax.fieldtesttool.tif;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.media.jai.PlanarImage;

import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;

public final class TifIO {
	
	public static BufferedImage read(URL fileUrl) throws FileNotFoundException,IOException {
		FileInputStream in = new FileInputStream(fileUrl.getFile());
	    FileChannel channel = in.getChannel();
	    ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());
	    channel.read(buffer);
	    return load(buffer.array());
		
	}
	
	  static BufferedImage load(byte[] data) throws IOException {
		  BufferedImage image = null;
	    SeekableStream stream = new ByteArraySeekableStream(data);
	    String[] names = ImageCodec.getDecoderNames(stream);
	    ImageDecoder dec = 
	      ImageCodec.createImageDecoder(names[0], stream, null);
	    RenderedImage im = dec.decodeAsRenderedImage();
	    image = PlanarImage.wrapRenderedImage(im).getAsBufferedImage();
	    return image;
	  }

}
