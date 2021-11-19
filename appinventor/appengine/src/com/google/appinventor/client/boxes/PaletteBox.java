// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Box implementation for palette panels.
 *
 */
public final class PaletteBox extends Box {

  // Singleton palette box instance
  private static final PaletteBox INSTANCE = new PaletteBox();
  public boolean loaded = false;

  /**
   * Return the palette box.
   *
   * @return  palette box
   */
  public static PaletteBox getPaletteBox() {
    return INSTANCE;
  }
  private final StackPanel palettePanel;
  private Map<String, VerticalPanel> panelsByCat;
//  private Map<String, PaletteEntry> paletteItems = new HashMap<>();
  private Map<String, SimplePaletteItem> paletteItems = new HashMap<>();
  DropTargetProvider dropTargetProvider;
  // Component database: information about components (including their properties and events)
  private final SimpleComponentDatabase COMPONENT_DATABASE;

  /**
   * Creates new palette box.
   */
  private PaletteBox() {
    super(MESSAGES.paletteBoxCaption(),
        200,       // height
        false,     // minimizable
        false,     // removable
        false,     // startMinimized
        false,     // usePadding
        false);    // highlightCaption

    palettePanel = new StackPanel();
    palettePanel.setWidth("100%");
    panelsByCat = new HashMap<>();

    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance();
    for (ComponentCategory cat: ComponentCategory.values()) {
      if (showCategory(cat)) {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panelsByCat.put(cat.getName(), panel);
        palettePanel.add(panel,
            ComponentsTranslation.getCategoryName(cat.getName()));
      }
    }

    setContent(palettePanel);
  }

  private static boolean showCategory(ComponentCategory category) {
    if (category == ComponentCategory.UNINITIALIZED) {
      return false;
    }
    if (category == ComponentCategory.INTERNAL &&
        !AppInventorFeatures.showInternalComponentsCategory()) {
      return false;
    }
    return true;
  }

  /*
   * Adds a component entry to the palette.
   */
  private void addPaletteEntry(SimplePaletteItem component, ComponentCategory category) {
    panelsByCat.get(category.getName()).add(component);

    // TODO: Determine function served by Helpers
//    if (panel == null) {
//      panel = addComponentCategory(category);
//    }
//    PaletteHelper paletteHelper = paletteHelpers.get(category);
//    if (paletteHelper != null) {
//      paletteHelper.addPaletteItem(panel, component);
//    } else {
//      panel.add(component);
//    }
  }

  public void loadComponents(DropTargetProvider dp) {
    dropTargetProvider = dp;
    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      this.addComponent(component);
    }
    loaded = true;
  }

  /**
   *  Loads a single Component to Palette. Used for adding Components.
   */

  public void addComponent(String componentName) {
    if (paletteItems.containsKey(componentName)) { // We are upgrading
      removeComponent(componentName);
    }
    int version = COMPONENT_DATABASE.getComponentVersion(componentName);
    String versionName = COMPONENT_DATABASE.getComponentVersionName(componentName);
    String dateBuilt = COMPONENT_DATABASE.getComponentBuildDate(componentName);
    String helpString = COMPONENT_DATABASE.getHelpString(componentName);
    String helpUrl = COMPONENT_DATABASE.getHelpUrl(componentName);
    String categoryDocUrlString = COMPONENT_DATABASE.getCategoryDocUrlString(componentName);
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentName);
    Boolean showOnPalette = COMPONENT_DATABASE.getShowOnPalette(componentName);
    Boolean nonVisible = COMPONENT_DATABASE.getNonVisible(componentName);
    Boolean external = COMPONENT_DATABASE.getComponentExternal(componentName);
    String iconName = COMPONENT_DATABASE.getIconName(componentName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    String componentType = COMPONENT_DATABASE.getComponentType(componentName);
    if (showOnPalette && showCategory(category)) {
//      PaletteEntry entry = new PaletteEntry(componentName, componentType, iconName, nonVisible,
//          external, version, versionName, dateBuilt, helpString, helpUrl, categoryDocUrlString,
//          external, "TODO-LicenseInfo", dropTargetProvider);
      SimplePaletteItem item = new SimplePaletteItem(
          new SimpleComponentDescriptor(componentName, version, versionName, dateBuilt, helpString, helpUrl,
              categoryDocUrlString, showOnPalette, nonVisible, external),
          dropTargetProvider);
      paletteItems.put(componentName, item);
      addPaletteEntry(item, category);

      // Make a second copy for the search mechanism
//      item = new SimplePaletteItem(
//          new SimpleComponentDescriptor(componentType, version, versionName, dateBuilt,
//              helpString, helpUrl, categoryDocUrlString, showOnPalette, nonVisible, external),
//          dropTargetProvider);
      // Handle extensions
//      if (external) {
//        translationMap.put(componentTypeName.toLowerCase(), componentTypeName);
//        requestRebuildList();
//      }
//      searchSimplePaletteItems.put(componentTypeName, item);
    }
  }

  public void removeComponent(String componentTypeName) {
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    if (paletteItems.containsKey(componentTypeName)) {
      removePaletteItem(paletteItems.get(componentTypeName), category);
      paletteItems.remove(componentTypeName);
    }
//    if (category == ComponentCategory.EXTENSION) {
//      searchSimplePaletteItems.remove(componentTypeName);
//      translationMap.remove(componentTypeName);
//      requestRebuildList();
//    }
  }

  private void removePaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel catPanel = panelsByCat.get(category);
    catPanel.remove(component);
    if (catPanel.getWidgetCount() < 1) {
      palettePanel.remove(catPanel);
      panelsByCat.remove(category);
    }
  }
}
