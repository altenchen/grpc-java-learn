package grpc.client;

public interface Functional<Arg, Result>
{
    Result run(Arg arg);
}