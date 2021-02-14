package org.destinationsol.assets.ui;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import org.terasology.gestalt.assets.format.AbstractAssetAlterationFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDeltaFileFormat;
import org.terasology.nui.UILayout;
import org.terasology.nui.UIWidget;
import org.terasology.nui.asset.UIData;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.reflection.WidgetLibrary;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

@RegisterAssetDeltaFileFormat
public class UIDeltaFormat extends AbstractAssetAlterationFileFormat<UIData> {
    private WidgetLibrary widgetLibrary;

    public UIDeltaFormat(WidgetLibrary library) {
        super("ui");
        widgetLibrary = library;
    }

    /**
     * Applies an alteration to the given assetData
     *
     * @param input     The input corresponding to this asset
     * @param assetData An assetData to update
     * @throws IOException If there are any errors loading the alteration
     */
    @Override
    public void apply(AssetDataFile input, UIData assetData) throws IOException {
        UIWidget rootWidget = assetData.getRootWidget();
        try (JsonReader reader = new JsonReader(new InputStreamReader(input.openStream(), Charsets.UTF_8))) {
            reader.setLenient(true);
            JsonElement rootElement = new JsonParser().parse(reader);
            merge(rootElement, rootWidget);
        }
    }

    private void merge(JsonElement rootElement, UIWidget rootWidget) throws IOException {
        if (rootElement.isJsonObject()) {
            JsonObject rootObject = rootElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> subObject : rootObject.entrySet()) {
                JsonElement subObjectElement = subObject.getValue();
                if (subObjectElement.isJsonObject()) {
                    // TODO
                    Iterator<UIWidget> widgetIterator = rootWidget.iterator();
                    if (widgetIterator.hasNext()) {
                        // TODO
                        merge(subObjectElement, widgetIterator.next());
                    } else {
                        throw new IOException("");
                    }
                } else if (subObjectElement.isJsonPrimitive()) {
                    JsonPrimitive subObjectPrimitive = subObjectElement.getAsJsonPrimitive();
                    if (subObjectPrimitive.isString() && subObject.getKey().equalsIgnoreCase(UIFormat.ID_FIELD)) {
                        rootWidget = rootWidget.find(subObjectPrimitive.getAsString(), UIWidget.class);
                        continue;
                    }

                    Class<? extends UIWidget> widgetClass = rootWidget.getClass();
                    try {
                        Field field = widgetClass.getField(subObject.getKey());
                        field.set(rootWidget, new Gson().fromJson(subObjectElement, field.getType()));
                    } catch (Exception ignore) {
                    }
                } else if (subObjectElement.isJsonArray()) {
                    JsonArray subObjectArray = subObjectElement.getAsJsonArray();
                    Gson gson = new Gson();
                    if (subObject.getKey().equalsIgnoreCase(UIFormat.CONTENTS_FIELD)) {
                        UIFormat uiFormat = new UIFormat(widgetLibrary);
                        for (int objectNo = 0; objectNo < subObjectArray.size(); objectNo++) {
                            UIData data = uiFormat.load(subObjectArray.get(objectNo));
                            ((UILayout<?>)rootWidget).addWidget(data.getRootWidget(), null);
                        }
                    } else {
                        Class<? extends UIWidget> widgetClass = rootWidget.getClass();
                        try {
                            Field field = widgetClass.getField(subObject.getKey());
                            int originalArraySize = Array.getLength(field.get(rootWidget));
                            Object array = Array.newInstance(field.getType(),
                                    originalArraySize + subObjectArray.size());

                            for (int originalElementNo = 0; originalElementNo < originalArraySize; originalElementNo++) {
                                Array.set(array, originalElementNo, Array.get(array, originalElementNo));
                            }
                            for (int objectNo = 0; objectNo < subObjectArray.size(); objectNo++) {
                                Array.set(array, subObjectArray.size() + objectNo,
                                        gson.fromJson(subObjectArray.get(objectNo), field.getType()));
                            }
                            field.set(rootWidget, array);
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
        }
    }
}
