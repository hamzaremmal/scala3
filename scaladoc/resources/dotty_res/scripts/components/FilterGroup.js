class FilterGroup extends Component {
  constructor(props) {
    super(props);

    this.filterToggleRef = findRef(".filterToggleButton");
    this.filtersContainerRef = findRef(".filtersContainer");

    withEvent(
      this.filterToggleRef,
      "click",
      this.props.onFilterVisibilityChange
    );

    this.render(this.props);
  }

  onFilterClick = ({
    currentTarget: {
      dataset: { key, value },
    },
  }) => {
    this.props.onFilterToggle(key, value);
  };

  onSelectAllClick = ({
    currentTarget: {
      dataset: { key },
    },
  }) => {
    this.props.onGroupSelectChange(key, true);
  };

  onDeselectAllClick = ({
    currentTarget: {
      dataset: { key },
    },
  }) => {
    this.props.onGroupSelectChange(key, false);
  };

  attachFiltersClicks() {
    const refs = findRefs(
      "li.filterButtonItem",
      this.filtersContainerRef
    );
    attachListeners(refs, "click", this.onFilterClick);
  }

  attachSelectingButtonsClicks() {
    const selectAllRefs = findRefs(
      "button.selectAll",
      this.filtersContainerRef
    );
    const deselectAllRefs = findRefs(
      "button.deselectAll",
      this.filtersContainerRef
    );

    attachListeners(selectAllRefs, "click", this.onSelectAllClick);
    attachListeners(deselectAllRefs, "click", this.onDeselectAllClick);
  }

  isActive(isActive) {
    return isActive ? "active" : "";
  }

  isVisible(visible) {
    return visible ? "visible" : "";
  }

  getSortedValues(filterKey, values) {
    const defaultFilterKey = `${filterKey.charAt(1).toLowerCase()}${filterKey.slice(2)}`
    const defaultGroupFilter = Filter.defaultFilters[defaultFilterKey]

    return Object.entries(values).sort(([a], [b]) =>  {
      if (a === defaultGroupFilter) {
        return -1
      }

      if (b === defaultGroupFilter) {
        return 1
      }

      return a.localeCompare(b)
    })
  }

  getFirstSelected(filterKey, values) {
    const sortedValues = this.getSortedValues(filterKey, values);
    return sortedValues.find(([_name, filterObject]) => filterObject.selected)[0] || "";
  }

  getNumberOfSelectedFilters = (filterKey, values) => {
    const sortedValues = this.getSortedValues(filterKey, values);
    return sortedValues.reduce((prev, curr) => {
      if(curr[1].selected) {
        return prev +1;
      }
      return prev
    }, 0)
  }

  getFilterGroup(filterKey, values) {
    const firstSelected = this.getFirstSelected(filterKey, values);
    const numberOfSelectedFilters = this.getNumberOfSelectedFilters(filterKey, values);
    const numberToDisplay = numberOfSelectedFilters > 1
      ? `+${numberOfSelectedFilters -1}`
      : ""

    return `
      <div class="pill-container body-small" tabindex="1">
        <div class="pill ${numberToDisplay !== "" ? "has-value" : ""}">
          <span class="filter-name">${filterKey.substring(1)}</span>
          ${firstSelected} ${numberToDisplay}
          <span class="icon-button close"/>
        </div>
        <ul>
          ${this.getSortedValues(filterKey, values)
            .map(
              ([key, data]) =>
              `<li
                class="filterButtonItem"
                data-selected="${data.selected}"
                data-test-id="filterGroupButton"
                data-key="${filterKey}"
                data-selected="${data.selected}"
                data-value="${key}"
              >
              ${key}
              </li>`
            )
          .join(" ")}
        </ul>
      </div>
    `;
  }

  render({ filter }) {
    attachDOM(
      this.filtersContainerRef,
      Object.entries(filter.filters)
        .filter(([_key, values]) => Object.values(values).some((v) => v.visible))
        .map(([key, values]) => this.getFilterGroup(key, values))
    );

    this.attachFiltersClicks();
    this.attachSelectingButtonsClicks();
  }
}
