import com.IceCreamQAQ.YuWeb.controller.render.RenderStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RenderText extends RenderStream {
    public RenderText(String text) {
        super(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }
}
