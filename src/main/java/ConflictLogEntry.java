/**
 * Class representing one conflict event in the log
 */
class ConflictLogEntry
{
    final int containerId;
    final String resourceId;

    public ConflictLogEntry(final int containerId,
                            final String resourceId)
    {
        this.containerId = containerId;
        this.resourceId  = resourceId;
    }
}