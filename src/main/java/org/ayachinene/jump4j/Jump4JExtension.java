package org.ayachinene.jump4j;

import com.maddyhome.idea.vim.command.MappingMode;
import com.maddyhome.idea.vim.extension.VimExtension;
import com.maddyhome.idea.vim.helper.StringHelper;
import org.jetbrains.annotations.NotNull;

import static com.maddyhome.idea.vim.extension.VimExtensionFacade.putExtensionHandlerMapping;
import static com.maddyhome.idea.vim.extension.VimExtensionFacade.putKeyMappingIfMissing;
import static org.ayachinene.jump4j.Utils.command;

public class Jump4JExtension implements VimExtension {

    @Override
    public @NotNull String getName() {
        return "jump4j";
    }

    @Override
    public void init() {
        putExtensionHandlerMapping(
                MappingMode.NXO,
                StringHelper.parseKeys(command("next-param")),
                getOwner(),
                new ParameterHandler(false),
                false
        );
        putKeyMappingIfMissing(
                MappingMode.NXO,
                StringHelper.parseKeys("gz"),
                getOwner(),
                StringHelper.parseKeys(command("next-param")),
                true
        );
        putExtensionHandlerMapping(
                MappingMode.NXO,
                StringHelper.parseKeys(command("prev-param")),
                getOwner(),
                new ParameterHandler(true),
                false
        );
        putKeyMappingIfMissing(
                MappingMode.NXO,
                StringHelper.parseKeys("gx"),
                getOwner(),
                StringHelper.parseKeys(command("prev-param")),
                true
        );
    }
}
