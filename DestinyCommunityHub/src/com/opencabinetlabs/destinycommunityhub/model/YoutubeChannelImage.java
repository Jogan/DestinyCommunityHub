package com.opencabinetlabs.destinycommunityhub.model;

import java.util.ArrayList;
import java.util.List;

public class YoutubeChannelImage {
	private List<Item> items = new ArrayList<Item>();

	public List<Item> getItems() {
		return items;
	}
	
	public Item getItem(){
		return items.get(0);
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}


	public class BrandingSettings {

		private Image image;

		public Image getImage() {
			return image;
		}

		public void setImage(Image image) {
			this.image = image;
		}

	}

	public class Image {

		private String bannerMobileImageUrl;

		public String getBannerMobileImageUrl() {
			return bannerMobileImageUrl;
		}

		public void setBannerMobileImageUrl(String bannerMobileImageUrl) {
			this.bannerMobileImageUrl = bannerMobileImageUrl;
		}

	}

	public class Item {

		private BrandingSettings brandingSettings;

		public BrandingSettings getBrandingSettings() {
			return brandingSettings;
		}

		public void setBrandingSettings(BrandingSettings brandingSettings) {
			this.brandingSettings = brandingSettings;
		}

	}
}
