<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import CustListTableV2 from '../customer/cust-list-table-v2.vue';

defineOptions({ name: 'BizWorkPublicPoolPage' });
</script>

<template>
  <Page>
    <CustListTableV2
      key="public-pool-customers"
      title="公海客户"
      public-pool-only
    />
  </Page>
</template>
