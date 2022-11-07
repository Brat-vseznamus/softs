package lab3.repository;

@FunctionalInterface
public interface ColumnsMapper<SE extends Entity, DE extends Entity> {
    DE map(SE entity);
}
