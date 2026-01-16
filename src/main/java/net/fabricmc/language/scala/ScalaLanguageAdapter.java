package net.fabricmc.language.scala;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;

@Slf4j
public class ScalaLanguageAdapter implements LanguageAdapter {

	@Override
	@SneakyThrows
	public <T> T create(ModContainer mod, String value, Class<T> type) {
		log.debug(String.format("Constructing for scala mod %s.", mod.getMetadata().getId()));

		for (String val : Stream.of(value, value + "$").collect(Collectors.toList())) {
			Optional<T> container = getScalaObject(mod, val, type);
			if (container.isPresent())
				return container.get();
		}

		throw new LanguageAdapterException(String.format("Unable to instantiate mod %s.", mod));
	}

	@SneakyThrows
	@SuppressWarnings("unchecked")
	private <T> Optional<T> getScalaObject(ModContainer mod, String value, Class<T> type) {
		Object obj = null;
		try {
			Class<?> clazz = Class.forName(value, true, ScalaLanguageAdapter.class.getClassLoader());
			obj = clazz.getField("MODULE$").get(null);
		} catch (NoSuchFieldException | ClassNotFoundException e) {
			log.debug(String.format("Reflecting failed for `%s`: %s", value, e));
			return Optional.empty();
		}

		Optional<T> container = Optional.empty();
		try {
			container = Optional.of((T) obj);
		} catch (ClassCastException e) {
			log.warn(String.format("Failed to cast object of `%s` into type `%s`: %s", obj, type, e));
		}
		return container;
	}
}
