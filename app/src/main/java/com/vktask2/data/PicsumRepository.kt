package com.vktask2.data

class PicsumRepository(private val api: PicsumApi) {
    private val pageCache = mutableMapOf<Int, List<Photo>>()

    suspend fun loadPage(page: Int, pageSize: Int): List<Photo> {
        if (page < 1) return emptyList()

        val cached = pageCache[page]
        if (cached != null) return cached

        val fresh = api.getPhotos(page, pageSize).map { it.toDomain() }
        pageCache[page] = fresh
        return fresh
    }
}

