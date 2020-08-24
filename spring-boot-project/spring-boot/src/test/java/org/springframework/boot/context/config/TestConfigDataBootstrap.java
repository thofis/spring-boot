/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

/**
 * Test classes used with
 * {@link ConfigDataEnvironmentPostProcessorBootstrapRegistryIntegrationTests} to show how
 * a bootstrap registry can be used. This example will create helper instances during
 * result and load. It also shows how the helper can ultimately be registered as a bean.
 *
 * @author Phillip Webb
 */
class TestConfigDataBootstrap {

	static class LocationResolver implements ConfigDataLocationResolver<Location> {

		@Override
		public boolean isResolvable(ConfigDataLocationResolverContext context, String location) {
			return location.startsWith("testbootstrap:");
		}

		@Override
		public List<Location> resolve(ConfigDataLocationResolverContext context, String location) {
			ResolverHelper helper = context.getBootstrapRegistry().get(ResolverHelper.class,
					() -> new ResolverHelper(location));
			return Collections.singletonList(new Location(helper));
		}

	}

	static class Loader implements ConfigDataLoader<Location> {

		@Override
		public ConfigData load(ConfigDataLoaderContext context, Location location) throws IOException {
			context.getBootstrapRegistry().get(LoaderHelper.class, () -> new LoaderHelper(location),
					LoaderHelper::addToContext);
			return new ConfigData(
					Collections.singleton(new MapPropertySource("loaded", Collections.singletonMap("test", "test"))));
		}

	}

	static class Location extends ConfigDataLocation {

		private final ResolverHelper resolverHelper;

		Location(ResolverHelper resolverHelper) {
			this.resolverHelper = resolverHelper;
		}

		@Override
		public String toString() {
			return "test";
		}

		ResolverHelper getResolverHelper() {
			return this.resolverHelper;
		}

	}

	static class ResolverHelper {

		private final String location;

		ResolverHelper(String location) {
			this.location = location;
		}

		String getLocation() {
			return this.location;
		}

	}

	static class LoaderHelper {

		private final Location location;

		LoaderHelper(Location location) {
			this.location = location;
		}

		Location getLocation() {
			return this.location;
		}

		static void addToContext(ConfigurableApplicationContext context, LoaderHelper loaderHelper) {
			context.getBeanFactory().registerSingleton("loaderHelper", loaderHelper);
		}

	}

}
