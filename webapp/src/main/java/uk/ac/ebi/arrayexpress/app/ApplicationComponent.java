package uk.ac.ebi.arrayexpress.app;

abstract public class ApplicationComponent
{
    private String componentName;

    public ApplicationComponent( String name )
    {
        componentName = name;
    }

    public String getName()
    {
        return componentName;
    }

    public ApplicationComponent getComponent( String name )
    {
        return Application.getInstance().getComponent(name);
    }

    public ApplicationPreferences getPreferences()
    {
        return Application.getInstance().getPreferences();
    }

    public abstract void initialize();

    public abstract void terminate();
}
