package ztqjntm.update.effect;

/**
 * 普通箭：命中后什么都不做，完全交给原版流程（插在地上、可以捡回）。
 * <p>
 * 单独留一个类而不是用 {@code null}，是为了让 {@code ModItems} 里每种箭都写着“效果是什么”，
 * 不必区分“没效果”和“忘了填”。
 */
public final class NoneEffect implements ArrowEffect {
    public static final NoneEffect INSTANCE = new NoneEffect();

    private NoneEffect() {
    }
}
