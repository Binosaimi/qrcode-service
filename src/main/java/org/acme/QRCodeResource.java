package org.acme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import io.quarkiverse.barcode.zxing.ZebraCrossing;
import io.smallrye.common.constraint.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/qrcode")
public class QRCodeResource {

    @GET
    @Produces("image/png")
    public Response generateQRCodeWithLogo(@QueryParam("qrCodeData") String qrCodeData, @QueryParam("channelId") @NotNull int channelId) throws Exception {
        String logo = (channelId == 1) ? "/logo.png" : "/logo2.png";

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        byte[] qrCodeBytes = ZebraCrossing.barcodetoPng(
                ZebraCrossing.qrCode(qrCodeData, 300, 300, hints));

        BufferedImage qrCodeImage = ImageIO.read(new ByteArrayInputStream(qrCodeBytes));

        try (InputStream logoStream = getClass().getResourceAsStream(logo)) {
            BufferedImage logoImage = ImageIO.read(logoStream);

            int logoWidth = qrCodeImage.getWidth() / 10;
            int logoHeight = qrCodeImage.getHeight() / 10;

            Image scaledLogo = logoImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
            BufferedImage bufferedLogo = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gLogo = bufferedLogo.createGraphics();
            gLogo.drawImage(scaledLogo, 0, 0, null);
            gLogo.dispose();

            int logoX = (qrCodeImage.getWidth() - logoWidth) / 2;
            int logoY = (qrCodeImage.getHeight() - logoHeight) / 2;

            Graphics2D g = qrCodeImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 3; // extra space around the logo
            g.setColor(Color.WHITE);
            g.fillRect(logoX - padding, logoY - padding, logoWidth + 2 * padding, logoHeight + 2 * padding);

            g.drawImage(bufferedLogo, logoX, logoY, null);

            g.dispose();

        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrCodeImage, "png", baos);
        byte[] outputBytes = baos.toByteArray();

        return Response.ok(outputBytes).build();
    }
}