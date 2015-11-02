package fr.lteconsulting.pomexplorer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class ProjectWatcher
{
    private final WatchService service;

    private final Path projectPath;

    private final Map<Path, WatchKey> keys = new HashMap<>();

    public ProjectWatcher(Path projectPath)
    {
        this.projectPath = projectPath;
        try
        {
            this.service = FileSystems.getDefault().newWatchService();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void register() throws IOException
    {
        watchPath(projectPath);

        System.out.println("registered project files...");
    }

    public boolean hasChanges()
    {
        WatchKey key = service.poll();

        processWatchKey(key);

        return key != null;
    }

    public boolean waitChange()
    {
        WatchKey key = null;
        try
        {
            key = service.take();
        } catch (InterruptedException e)
        {
            System.out.println("Interrupted while waiting changes on files...");
            e.printStackTrace();
        }

        processWatchKey(key);

        return key != null;
    }

    private void processWatchKey(WatchKey key)
    {
        if (key == null)
            return;

        key.reset();

        for (WatchEvent<?> event : key.pollEvents())
        {
            Path eventTarget = Paths.get(key.watchable().toString(), event.context().toString()).toAbsolutePath();
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE)
            {
                System.out.println("=> created " + eventTarget.toString());

                watchPath(Paths.get(projectPath.toString(), event.context().toString()));
            }
            else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE)
            {
                System.out.println("=> deleted " + eventTarget.toString());

                unwatchPath(Paths.get(projectPath.toString(), event.context().toString()));
            }
            else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY)
            {
                System.out.println("=> modified " + eventTarget.toString());
            }
        }
    }

    private void watchPath(Path path)
    {
        if (path == null)
            return;

        watchPath(path.toFile());
        watchPathRec(Paths.get(path.toAbsolutePath().toString(), "src").toFile());
    }

    private void watchPath(File file)
    {
        if (file == null || !file.exists() || !file.isDirectory())
            return;

        Path path = file.toPath();

        if (keys.containsKey(path))
        {
            System.out.println("warning : already watched " + path.toString());
            return;
        }

        if (!shouldBeWatched(file))
        {
            System.out.println("warning : not watched directory " + file.getAbsolutePath());
            return;
        }

        try
        {
            System.out.println("watch : " + path.toString());
            WatchKey key = path.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            keys.put(path, key);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void watchPathRec(File file)
    {
        watchPath(file);

        File[] files = file.listFiles();
        if (files != null)
        {
            for (File child : files)
                watchPathRec(child);
        }
    }

    private void unwatchPath(Path path)
    {
        if (path == null || !path.toFile().isDirectory())
            return;

        WatchKey key = keys.get(path);
        if (key == null)
        {
            System.out.println("warning : not watched path " + path + ", nothing to do...");
            return;
        }

        key.cancel();
        keys.remove(path);
    }

    /**
     * Search in the .gitignore file and other clues to know if a directory
     * should be watched
     *
     * @param file
     * @return
     */
    private boolean shouldBeWatched(File file)
    {
        // if( file.getName().equals( "target" ) )
        // return false;

        File current = file.getParentFile();
        Path remaining = Paths.get(file.getName());
        while (current != null)
        {
            File gitignore = Paths.get(current.getAbsolutePath(), ".gitignore").toFile();
            if (gitignore != null && gitignore.exists() && gitignore.isFile())
            {
                try
                {
                    for (String line : Files.readAllLines(gitignore.toPath()))
                    {
                        if (line.contains("*") || line.contains("//") || line.contains("?"))
                            continue;

                        if (remaining.compareTo(Paths.get(line)) == 0)
                            return false;
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            remaining = Paths.get(current.getName(), remaining.toString());
            current = current.getParentFile();
        }

        return true;
    }
}
