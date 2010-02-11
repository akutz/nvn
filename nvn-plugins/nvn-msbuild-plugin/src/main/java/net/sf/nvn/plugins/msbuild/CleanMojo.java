package net.sf.nvn.plugins.msbuild;


/**
 * A Maven plug-in for cleaning .NET solutions and/or projects with MSBuild.
 * 
 * @goal clean
 * @phase clean
 * @description A Maven plug-in for cleaning .NET solutions and/or projects with
 *              MSBuild.
 */
public class CleanMojo extends MSBuildMojo
{
    @Override
    void initTargets()
    {
        if (this.targets != null && this.targets.length > 0)
        {
            return;
        }

        this.targets = new String[]
        {
            "Clean"
        };
    }
}
