package fr.atlasworld.cardinal.plugin;

import me.lucko.spark.common.util.classfinder.ClassFinder;

import java.util.HashSet;
import java.util.Set;

public class GroupClassLoader extends ClassLoader {
    private final ClassLoader parent;
    private final Set<PluginClassLoader> pluginClassLoaders;

    public GroupClassLoader(ClassLoader parent) {
        this.parent = parent;
        this.pluginClassLoaders = new HashSet<>();
    }

    public void registerPluginClassLoader(PluginClassLoader pluginClassLoader) {
        this.pluginClassLoaders.add(pluginClassLoader);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return this.loadClass(name, resolve, true, true);
    }

    /**
     * Load a class from the group class loader.
     *
     * @param name         name of the class.
     * @param checkPlugins whether to look inside the plugin class loaders.
     * @param checkParent  whether to look inside the parent class loader.
     * @return the loaded class.
     * @throws ClassNotFoundException if the class could not be found.
     */
    public Class<?> loadClass(String name, boolean resolve, boolean checkParent, boolean checkPlugins) throws ClassNotFoundException {
        if (checkParent) {
            try {
                return this.parent.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (checkPlugins) {
            for (PluginClassLoader loader : this.pluginClassLoaders) {
                try {
                    return loader.loadClass(name, resolve, false, false);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        throw new ClassNotFoundException(name);
    }

    public ClassFinder asClassFinder() {
        return name -> {
            try {
                return this.loadClass(name, false, true, true);
            } catch (ClassNotFoundException e) {
                return null;
            }
        };
    }
}
