package net.pl3x.map.core.world;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TagType;
import de.bluecolored.bluenbt.TypeDeserializer;

import net.pl3x.map.core.Pl3xMap;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads one entry of a chunk section's block palette.
 *
 * <p>Final Station patch for Minecraft 26.3, which changed how palette entries are saved. Three
 * shapes exist now, on top of the old one:
 *
 * <p>old (up to 26.2): {Name: "minecraft:oak_log", Properties: {axis: "y"}} 26.3, with states: {id:
 * "minecraft:oak_log", properties: {axis: "y"}} 26.3, plain block: "minecraft:stone" 26.3, plain
 * block inside a mixed list: {"": "minecraft:stone"}
 */
public class BlockStateDeserializer implements TypeDeserializer<BlockState> {
    private static final Map<String, String> DEFAULT_PROPERTIES =
            Map.of("age", "0", "moisture", "0", "power", "0");

    public BlockState read(NBTReader reader) throws IOException {
        // 26.3: a block without any states is saved as just its name.
        if (reader.peek() == TagType.STRING) {
            return state(reader.nextString(), null);
        }

        reader.beginCompound();
        String id = null;
        Map<String, String> properties = null;

        while (reader.hasNext()) {
            String key = reader.name();
            switch (key) {
                case "Name": // up to 26.2
                case "id": // 26.3
                case "": // 26.3, plain block wrapped because the list also holds blocks with states
                    if (reader.peek() == TagType.STRING) {
                        id = reader.nextString();
                    } else {
                        reader.skip();
                    }
                    break;
                case "Properties": // up to 26.2
                case "properties": // 26.3
                    if (reader.peek() == TagType.COMPOUND) {
                        properties = new LinkedHashMap<>();
                        reader.beginCompound();

                        while (reader.hasNext()) {
                            String property = reader.name();
                            if (reader.peek() == TagType.STRING) {
                                properties.put(property, reader.nextString());
                            } else {
                                reader.skip();
                            }
                        }

                        reader.endCompound();
                    } else {
                        reader.skip();
                    }
                    break;
                default:
                    reader.skip();
            }
        }

        reader.endCompound();
        return state(id, properties);
    }

    private static BlockState state(String id, Map<String, String> properties) {
        if (id == null) {
            return Blocks.AIR.getDefaultState();
        }
        Block block = (Block) Pl3xMap.api().getBlockRegistry().getOrDefault(id, Blocks.AIR);
        return new BlockState(block, properties == null ? DEFAULT_PROPERTIES : properties);
    }
}
