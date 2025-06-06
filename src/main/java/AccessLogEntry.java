/**
 * Class representing one access event in the log
 */
class AccessLogEntry
{
    final int containerId;
    final String resourceId;
    final long acquireTime;
    final int processingTime;
    final long totalTime;

    public AccessLogEntry(final int containerId,
                          final String resourceId,
                          final long acquireTime,
                          final int processingTime,
                          final long totalTime)
    {
        this.containerId    = containerId;
        this.resourceId     = resourceId;
        this.acquireTime    = acquireTime;
        this.processingTime = processingTime;
        this.totalTime      = totalTime;
    }
}