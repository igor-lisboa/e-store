package br.uff.store.application.controllers;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** Maps stable seeded category IDs to the catalog checkbox selections. */
final class CatalogCategorySelection {
    private CatalogCategorySelection() { }

    static void apply(HttpServletRequest request, HttpSession session,
            List<String> categories, List<String> subcategories) {
        String value = request.getParameter("categoryId");
        if (value == null) {
            return;
        }
        int categoryId;
        try {
            categoryId = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return;
        }
        String platform;
        String type = null;
        if (categoryId >= 1 && categoryId <= 3) {
            platform = new String[] {"playstation", "xbox", "wii"}[categoryId - 1];
        } else if (categoryId >= 4 && categoryId <= 12) {
            platform = new String[] {"playstation", "xbox", "wii"}[(categoryId - 4) / 3];
            type = new String[] {"accessories", "consoles", "games"}[(categoryId - 4) % 3];
        } else {
            return;
        }
        select(platform, categories, session);
        if (type != null) {
            select(type, subcategories, session);
        }
    }

    private static void select(String value, List<String> selections, HttpSession session) {
        if (!selections.contains(value)) {
            selections.add(value);
        }
        session.setAttribute(value, "checked");
    }
}
