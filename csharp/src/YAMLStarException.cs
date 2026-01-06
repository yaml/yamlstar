namespace YAMLStar;

public class YAMLStarException : Exception
{
    public YAMLStarException(string message)
        : base(message)
    {
    }

    public YAMLStarException(string message, Exception innerException)
        : base(message, innerException)
    {
    }
}
